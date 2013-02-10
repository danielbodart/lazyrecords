package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.totallylazy.Sequences.sequence;

public class OuterJoin extends Join {
    public OuterJoin(Sequence<Record> records, Joiner using) {
        super(records, using);
    }

    public Iterable<Record> call(Record record) throws Exception {
        Sequence<Record> matches = records.filter(using.call(record));
        if (matches.isEmpty())
            return sequence(record);
        return matches.map(merge(record));
    }

    public static Join outerJoin(final Sequence<Record> records, final Joiner using) {
        return new OuterJoin(records, using);
    }
}