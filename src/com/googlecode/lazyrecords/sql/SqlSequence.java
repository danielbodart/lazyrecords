package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.CompoundExpression;
import com.googlecode.lazyrecords.sql.expressions.Expressible;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.totallylazy.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Pair.pair;
import static java.lang.String.format;

public class SqlSequence<T> extends Sequence<T> implements Expressible {
    private final SqlRecords sqlRecords;
    private final ExpressionBuilder selectBuilder;
    private final Logger logger;
    private final Value<Sequence<T>> data;
    private final Callable1<? super Record, ? extends T> callable;

    public SqlSequence(final SqlRecords records, final ExpressionBuilder selectBuilder, final Logger logger, Callable1<? super Record, ? extends T> callable) {
        this.sqlRecords = records;
        this.selectBuilder = selectBuilder;
        this.logger = logger;
        this.callable = callable;
        this.data = new Function<Sequence<T>>() {
            @Override
            public Sequence<T> call() throws Exception {
                return execute(selectBuilder);
            }
        }.lazy();
    }

    public Iterator<T> iterator() {
        return data.value().iterator();
    }

    private Sequence<T> execute(final ExpressionBuilder builder) {
        return sqlRecords.query(builder.build(), builder.fields()).map(callable);
    }

    private SqlSequence<T> build(final ExpressionBuilder builder) {
        return new SqlSequence<T>(sqlRecords, builder, logger, callable);
    }

    private <S> SqlSequence<S> build(final Keyword<S> keyword) {
        return new SqlSequence<S>(sqlRecords, selectBuilder.select(keyword), logger, new RecordTo<S>() {
            public S call(Record record) throws Exception {
                return record.get(keyword);
            }
        });
    }

    @Override
    public <S> Sequence<S> map(Callable1<? super T, ? extends S> callable) {
        if (callable instanceof ClientComputation) return super.map(callable);

        Callable1 raw = (Callable1) callable;
        if (raw instanceof Keyword) {
            return build(Unchecked.<Keyword<S>>cast(raw));
        }
        if (raw instanceof SelectCallable) {
            return Unchecked.cast(build(selectBuilder.select(((SelectCallable) raw).keywords())));
        }
        if (raw instanceof ReducingRecordsMapper) {
            ReducingRecordsMapper reducingRecordsMapper = (ReducingRecordsMapper) raw;
            final SqlSequence<SqlGroup<S>> groups = Unchecked.cast(build(selectBuilder.select(reducingRecordsMapper.aggregates())));
            return Unchecked.cast(groups);
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'map', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.map(callable);
    }

    @Override
    public <S> Sequence<S> flatMap(Callable1<? super T, ? extends Iterable<? extends S>> callable) {
        if (callable instanceof ClientComputation) return super.flatMap(callable);

        Callable1 raw = (Callable1) callable;
        if (raw instanceof Join) {
            return Unchecked.cast(build(selectBuilder.join((Join) raw)));
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'flatMap', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.flatMap(callable);
    }

    @Override
    public Sequence<T> filter(Predicate<? super T> predicate) {
        if (callable instanceof ClientComputation) return super.filter(predicate);

        try {
            return build(selectBuilder.filter(Unchecked.<Predicate<Record>>cast(predicate)));
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported predicate passed to 'filter', moving computation to client"), pair(Loggers.PREDICATE, predicate)));
            return super.filter(predicate);
        }
    }

    @Override
    public Option<T> find(Predicate<? super T> predicate) {
        return filter(predicate).headOption();
    }

    @Override
    public Sequence<T> sortBy(Comparator<? super T> comparator) {
        if (callable instanceof ClientComputation) return super.sortBy(comparator);

        try {
            return build(selectBuilder.orderBy(Unchecked.<Comparator<Record>>cast(comparator)));
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported comparator passed to 'sortBy', moving computation to client"), pair(Loggers.COMPARATOR, comparator)));
            return super.sortBy(comparator);
        }
    }

    @Override
    public Sequence<T> drop(int count) {
        try {
            return build(selectBuilder.offset(count));
        } catch (UnsupportedOperationException e) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, String.format("Unsupported operation in 'drop', moving computation to client due to exception: %s", e.getMessage()))));
            return super.drop(count);
        }
    }

    @Override
    public Sequence<T> take(int count) {
        try {
            return build(selectBuilder.fetch(count));
        } catch (UnsupportedOperationException e) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, String.format("Unsupported operation in 'take', moving computation to client due to exception: %s", e.getMessage()))));
            return super.take(count);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S reduce(Callable2<? super S, ? super T, ? extends S> callable) {
        if (callable instanceof ClientComputation) return super.reduce(callable);

        try {
            if (callable instanceof Reducer) {
                Reducer<?, ?> reducer = (Reducer) callable;
                ExpressionBuilder builder = selectBuilder.reduce(reducer);
                if (reducer instanceof Aggregates) return Unchecked.<S>cast(build(builder).head());
                SqlSequence<Record> records = new SqlSequence<Record>(sqlRecords, builder, logger, Functions.<Record>identity());
                return (S) records.head().fields().head().second();
            }
        } catch (UnsupportedOperationException ignored) {
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'reduce', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.reduce(callable);
    }

    @Override
    public int size() {
        final ImmutableKeyword<Number> rowCount = keyword("row_count", Number.class);
        final CompoundExpression countExpression = new CompoundExpression(textOnly(format("select count(*) %s from (", rowCount.name())), selectBuilder.build(), textOnly(")"));
        return sqlRecords.query(countExpression, Sequences.<Keyword<?>>sequence(rowCount)).map(rowCount).head().intValue();
    }

    @Override
    public <S extends Set<T>> S toSet(S set) {
        return Sets.set(set, execute(selectBuilder.distinct()));
    }

    @Override
    public Sequence<T> unique() {
        return build(selectBuilder.distinct());
    }

    @Override
    public String toString() {
        return selectBuilder.toString();
    }

    @Override
    public Expression build() {
        return selectBuilder;
    }

    @Override
    public boolean exists(Predicate<? super T> predicate) {
        return !filter(predicate).map(Unchecked.<Callable1<T, Integer>>cast(SqlSchema.one)).unique().isEmpty();
    }

    @Override
    public <K> Sequence<Group<K, T>> groupBy(final Callable1<? super T, ? extends K> callable) {
        if (callable instanceof Keyword) {
            final Keyword<K> keyword = (Keyword) callable;
            return Unchecked.cast(new SqlSequence<SqlGroup<K>>(sqlRecords, selectBuilder.groupBy(keyword), logger, new Callable1<Record, SqlGroup<K>>() {
                @Override
                public SqlGroup<K> call(Record record) throws Exception {
                    return new SqlGroup<K>(record.get(keyword), record);
                }
            }));
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'groupBy', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.groupBy(callable);
    }

    public static class SqlGroup<K> extends Group<K, Record> implements Record {
        private final Record record;

        public SqlGroup(K groupKey, Record record) {
            super(groupKey, Sequences.one(record));
            this.record = record;
        }

        @Override
        public <S> S get(Keyword<S> keyword) {
            return record.get(keyword);
        }

        @Override
        public <S> Option<S> getOption(Keyword<S> keyword) {
            return record.getOption(keyword);
        }

        @Override
        public <S> Record set(Keyword<S> name, S value) {
            return record.set(name, value);
        }

        @Override
        public Sequence<Pair<Keyword<?>, Object>> fields() {
            return record.fields();
        }

        @Override
        public Sequence<Keyword<?>> keywords() {
            return record.keywords();
        }

        @Override
        public <S> Sequence<S> valuesFor(Sequence<? extends Keyword<? extends S>> keywords) {
            return record.valuesFor(keywords);
        }
    }

}
