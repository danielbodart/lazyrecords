package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sets;
import com.googlecode.lazyrecords.sql.expressions.Expressible;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.SelectBuilder;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.Value;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Pair.pair;
import static java.lang.String.format;

public class SqlSequence extends Sequence<Record> implements Expressible {
    private final SqlRecords sqlRecords;
    private final SelectBuilder select;
    private final Logger logger;
    private final Value<Iterable<Record>> data;

    public SqlSequence(final SqlRecords records, final SelectBuilder select, final Logger logger) {
        this.sqlRecords = records;
        this.select = select;
        this.logger = logger;
        this.data = new Function<Iterable<Record>>() {
            @Override
            public Iterable<Record> call() throws Exception {
                return execute(select);
            }
        }.lazy();
    }

    public Iterator<Record> iterator() {
        return data.value().iterator();
    }

    private Iterable<Record> execute(final SelectBuilder select) {
        return sqlRecords.query(select.build(), select.select());
    }

    private SqlSequence build(final SelectBuilder builder){
        return new SqlSequence(sqlRecords, builder, logger);
    }

    @Override
    public <S> Sequence<S> map(final Callable1<? super Record, ? extends S> callable) {
        if (callable instanceof Keyword) {
            return new SingleValueSequence<S>(sqlRecords, select.select((Keyword) callable), callable, logger);
        }
        Callable1 raw = (Callable1) callable;
        if (raw instanceof SelectCallable) {
            return Unchecked.cast(build(select.select(((SelectCallable) raw).keywords())));
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'map', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.map(callable);
    }

    @Override
    public <S> Sequence<S> flatMap(Callable1<? super Record, ? extends Iterable<? extends S>> callable) {
        Callable1 raw = (Callable1) callable;
        if(raw instanceof Join){
            return Unchecked.cast(build(select.join(some((Join) raw))));
        }
        logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'flatMap', moving computation to client"), pair(Loggers.FUNCTION, callable)));
        return super.flatMap(callable);
    }

    @Override
    public Sequence<Record> filter(Predicate<? super Record> predicate) {
        try {
            return build(select.where(predicate));
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported predicate passed to 'filter', moving computation to client"), pair(Loggers.PREDICATE, predicate)));
            return super.filter(predicate);
        }
    }

    @Override
    public Sequence<Record> sortBy(Comparator<? super Record> comparator) {
        try {
            return build(select.orderBy(comparator));
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported comparator passed to 'sortBy', moving computation to client"), pair(Loggers.COMPARATOR, comparator)));
            return super.sortBy(comparator);
        }
    }

    @Override
    public <S> S reduce(Callable2<? super S, ? super Record, ? extends S> callable) {
        try {
            return Unchecked.cast(build(select.reduce(callable)).head());
        } catch (UnsupportedOperationException ex) {
            logger.log(Maps.map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.MESSAGE, "Unsupported function passed to 'reduce', moving computation to client"), pair(Loggers.FUNCTION, callable)));
            return super.reduce(callable);
        }
    }

    @Override
    public int size() {
        return ((Number) build(select.count()).head().fields().head().second()).intValue();
    }

    @Override
    public <S extends Set<Record>> S toSet(S set) {
        return Sets.set(set, execute(select.distinct()));
    }

    @Override
    public Sequence<Record> unique() {
        return build(select.distinct());
    }

    @Override
    public String toString() {
        return select.toString();
    }

    public Expression express() {
        return select;
    }
}
