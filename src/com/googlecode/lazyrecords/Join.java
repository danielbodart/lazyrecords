package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Unchecked.cast;

public abstract class Join implements Callable1<Record, Iterable<Record>> {
    protected final Sequence<Record> records;
    protected final Joiner using;

    protected Join(Sequence<Record> records, Joiner using) {
        this.records = records;
        this.using = cast(using);
    }

    public Sequence<Record> records() {
        return records;
    }

    public Joiner joiner() {
        return using;
    }

    public static Join join(Sequence<Record> records, Joiner using) {
        return InnerJoin.innerJoin(records, using);
    }
}
