package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import static com.googlecode.lazyrecords.Record.functions.merge;


public class InnerJoin extends Join {

	public InnerJoin(Sequence<Record> records, Callable1<? super Record, Predicate<Record>> using) {
		super(records, Unchecked.<Callable1<? super Record, Predicate<Record>>>cast(using));
    }

    public Iterable<Record> call(Record record) throws Exception {
        return records.filter(using.call(record)).map(merge(record));
    }

    public static Callable1<Record, Iterable<Record>> innerJoin(final Sequence<Record> records, final Callable1<? super Record, Predicate<Record>> using) {
        return new InnerJoin(records, using);
    }

}