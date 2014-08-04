package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

public class ReducingRecordsMapper extends Function1<Sequence<Record>, Record> {
    private Aggregates aggregates;

    public ReducingRecordsMapper(Aggregates aggregates) {
        this.aggregates = aggregates;
    }

    @Override
    public Record call(Sequence<Record> records) throws Exception {
        return records.reduce(aggregates);
    }

    public Sequence<Aggregate<Object, Object>> aggregates() {
        return aggregates.value();
    }
}
