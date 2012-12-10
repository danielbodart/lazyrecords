package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Aggregates extends ReducerFunction<Record, Record> implements Value<Sequence<Aggregate<Object, Object>>> {
    private final Sequence<Aggregate<Object, Object>> aggregates;

    private Aggregates(final Sequence<Aggregate<Object, Object>> aggregates) {
        this.aggregates = aggregates;
    }

    @Override
    public Record call(final Record accumulator, final Record nextRecord) throws Exception {
        return aggregateRecord(new Function1<Aggregate<Object, Object>, Object>() {
            @Override
            public Object call(Aggregate<Object, Object> aggregate) throws Exception {
                Object current = accumulator.get(aggregate);
                Object next = nextRecord.get(aggregate.source());
                return aggregate.call(current, next);
            }
        });
    }

    @Override
    public Record identity() {
        return aggregateRecord(new Function1<Aggregate<Object, Object>, Object>() {
            @Override
            public Object call(Aggregate<Object, Object> aggregate) throws Exception {
                return aggregate.identity();
            }
        });
    }

    private Record aggregateRecord(final Function1<Aggregate<Object, Object>, Object> valueFunc) {
        return record(aggregates.map(new Function1<Aggregate<Object, Object>, Pair<Keyword<?>, Object>>() {
            @Override
            public Pair<Keyword<?>, Object> call(Aggregate<Object, Object> aggregate) throws Exception {
                return Pair.<Keyword<?>, Object>pair(aggregate, valueFunc.call(aggregate));
            }
        }));
    }

    public Sequence<Aggregate<Object, Object>> value() {
        return aggregates;
    }

    public static Aggregates to(final Aggregate<?, ?>... aggregates) {
        return aggregates(sequence(aggregates));
    }

    public static Aggregates aggregates(final Sequence<Aggregate<?, ?>> sequence) {
        return new Aggregates(sequence.<Aggregate<Object, Object>>unsafeCast());
    }
}