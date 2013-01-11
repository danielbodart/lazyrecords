package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.sql.expressions.Expressible;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.SelectBuilder;
import com.googlecode.totallylazy.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Pair.pair;

public class SqlSequence<T> extends Sequence<T> implements Expressible {
    private final SqlRecords sqlRecords;
    private final SelectBuilder select;
    private final Logger logger;
    private final Value<Sequence<T>> data;
    private final Callable1<? super Record, ? extends T> callable;

    public SqlSequence(final SqlRecords records, final SelectBuilder select, final Logger logger, Callable1<? super Record, ? extends T> callable) {
        this.sqlRecords = records;
        this.select = select;
        this.logger = logger;
        this.callable = callable;
        this.data = new Function<Sequence<T>>() {
            @Override
            public Sequence<T> call() throws Exception {
                return execute(select);
            }
        }.lazy();
    }

    public Iterator<T> iterator() {
        return data.value().iterator();
    }

    private Sequence<T> execute(final SelectBuilder select) {
        return sqlRecords.query(select.build(), select.select()).map(callable);
    }

    private SqlSequence<T> build(final SelectBuilder builder) {
        return new SqlSequence<T>(sqlRecords, builder, logger, callable);
    }

    private <S> SqlSequence<S> build(final Keyword<S> keyword) {
        return new SqlSequence<S>(sqlRecords, select.select(keyword), logger, new FromRecord<S>() {
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
            return Unchecked.cast(build(select.select(((SelectCallable) raw).keywords())));
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'map', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.map(callable);
    }

    @Override
    public <S> Sequence<S> flatMap(Callable1<? super T, ? extends Iterable<? extends S>> callable) {
        Callable1 raw = (Callable1) callable;
        if (raw instanceof Join) {
            return Unchecked.cast(build(select.join(some((Join) raw))));
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'flatMap', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.flatMap(callable);
    }

    @Override
    public Sequence<T> filter(Predicate<? super T> predicate) {
        try {
            return build(select.where(Unchecked.<Predicate<Record>>cast(predicate)));
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
            return build(select.orderBy(Unchecked.<Comparator<Record>>cast(comparator)));
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported comparator passed to 'sortBy', moving computation to client"), pair(Loggers.COMPARATOR, comparator)));
            return super.sortBy(comparator);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S reduce(Callable2<? super S, ? super T, ? extends S> callable) {
        try {
            Callable2 raw = callable;
            SelectBuilder reduce = select.reduce(callable);
            if (raw instanceof Aggregates) {
                return Unchecked.cast(build(reduce).head());
            }
            SqlSequence<Record> records = new SqlSequence<Record>(sqlRecords, reduce, logger, Functions.<Record>identity());
            return (S) records.head().fields().head().second();
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'reduce', moving computation to client"), pair(Loggers.FUNCTION, callable)));
            return super.reduce(callable);
        }
    }

    @Override
    public int size() {
        T head = build(select.count()).head();
        Record record = (Record) head;
        return ((Number) record.fields().head().second()).intValue();
    }

    @Override
    public <S extends Set<T>> S toSet(S set) {
        return Sets.set(set, execute(select.distinct()));
    }

    @Override
    public Sequence<T> unique() {
        return build(select.distinct());
    }

    @Override
    public String toString() {
        return select.toString();
    }

    @Override
    public Expression express() {
        return select;
    }

    @Override
    public boolean exists(Predicate<? super T> predicate) {
        return !filter(predicate).map((Callable1<T, Integer>) SqlSchema.one).unique().isEmpty();
    }
}
