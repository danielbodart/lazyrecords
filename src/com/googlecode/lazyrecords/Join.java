package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.totallylazy.Unchecked.cast;


public class Join implements Callable1<Record, Iterable<Record>> {
    private final Sequence<Record> records;
    private final Callable1<Record, Predicate<Record>> using;

    public Join(Sequence<Record> records, Callable1<? super Record, Predicate<Record>> using) {
        this.records = records;
        this.using = cast(using);
    }

    public Iterable<Record> call(Record record) throws Exception {
        return records.filter(using.call(record)).map(merge(record));
    }

    public static Callable1<Record, Iterable<Record>> join(final Sequence<Record> records, final Callable1<? super Record, Predicate<Record>> using) {
        return new Join(records, using);
    }

    public Sequence<Record> records() {
        return records;
    }

    public Callable1<Record, Predicate<Record>> using() {
        return using;
    }
}