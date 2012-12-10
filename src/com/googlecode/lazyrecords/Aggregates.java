package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Aggregates extends ReducerFunction<Record, Record> implements Value<Sequence<Aggregate<?,?>>> {
    private final Sequence<Aggregate<?,?>> aggregates;

    public Aggregates(final Sequence<Aggregate<?,?>> aggregates) {
        this.aggregates = aggregates;
    }

    public Record call(Record accumulator, final Record nextRecord) throws Exception {
        return aggregates.fold(accumulator, new Callable2<Record, Aggregate<?, ?>, Record>() {
            @Override
            public Record call(Record record, Aggregate<?, ?> aggregate) throws Exception {
                Object current = accumulatorValue(record, aggregate);
                Object next = nextRecord.get(aggregate.source());
                Aggregate<Object, Object> cast = Unchecked.cast(aggregate);
                return record.set(cast, cast.call(current, next));
            }
        });
    }

    private Object accumulatorValue(Record record, Aggregate<?,?> aggregate) {
        Object value = record.get(aggregate.source());
        if (value == null) {
            return record.get(aggregate);
        }
        return value;
    }


    public Sequence<Aggregate<?,?>> value() {
        return aggregates;
    }

    public static Aggregates to(final Aggregate<?,?>... aggregates) {
        return aggregates(sequence(aggregates));
    }

    public static Aggregates aggregates(final Sequence<Aggregate<?,?>> sequence) {
        return new Aggregates(sequence);
    }

    @Override
    public Record identity() {
        return Record.constructors.record(aggregates.map(new Function1<Aggregate<?, ?>, Pair<Keyword<?>, Object>>() {
            @Override
            public Pair<Keyword<?>, Object> call(Aggregate<?, ?> aggregate) throws Exception {
                return Pair.<Keyword<?>, Object>pair(aggregate, aggregate.identity());
            }
        }));
    }
}
