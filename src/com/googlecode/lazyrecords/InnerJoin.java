package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.Record.functions.merge;


public class InnerJoin extends Join {
    private InnerJoin(Sequence<Record> records, Joiner using) {
        super(records, using);
    }

    public Iterable<Record> call(Record record) throws Exception {
        return records.filter(using.call(record)).map(merge(record));
    }

    public static Join innerJoin(final Sequence<Record> records, final Joiner using) {
        return new InnerJoin(records, using);
    }

}