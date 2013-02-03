package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Aggregates;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.lazyrecords.SelectCallable;
import com.googlecode.lazyrecords.sql.expressions.Expressible;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Functions;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sets;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.Value;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import static com.googlecode.totallylazy.Pair.pair;

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
        Callable1 raw = (Callable1) callable;
        if (raw instanceof Keyword) {
            return build(Unchecked.<Keyword<S>>cast(raw));
        }
        if (raw instanceof SelectCallable) {
            return Unchecked.cast(build(selectBuilder.select(((SelectCallable) raw).keywords())));
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'map', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.map(callable);
    }

    @Override
    public <S> Sequence<S> flatMap(Callable1<? super T, ? extends Iterable<? extends S>> callable) {
//        Callable1 raw = (Callable1) callable;
//        if (raw instanceof Join) {
//            Join join = (Join) raw;
//            ExpressionBuilder joined = JoinBuilder.join(selectBuilder, join);
//            return Unchecked.cast(build(joined));
//        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'flatMap', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.flatMap(callable);
    }

    @Override
    public Sequence<T> filter(Predicate<? super T> predicate) {
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
        try {
            return build(selectBuilder.orderBy(Unchecked.<Comparator<Record>>cast(comparator)));
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported comparator passed to 'sortBy', moving computation to client"), pair(Loggers.COMPARATOR, comparator)));
            return super.sortBy(comparator);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S reduce(Callable2<? super S, ? super T, ? extends S> callable) {
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
        T head = build(selectBuilder.count()).head();
        Record record = (Record) head;
        return ((Number) record.fields().head().second()).intValue();
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
        return !filter(predicate).map((Callable1<T, Integer>) SqlSchema.one).unique().isEmpty();
    }
}
