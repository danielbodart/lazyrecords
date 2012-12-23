package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.totallylazy.Sequences.sequence;


public class LeftJoin extends Join {
	public LeftJoin(Sequence<Record> records, Callable1<? super Record, Predicate<Record>> using) {
		super(records, Unchecked.<Callable1<? super Record, Predicate<Record>>>cast(using));
	}

	public Iterable<Record> call(Record record) throws Exception {
		Sequence<Record> matches = records.filter(using.call(record));
		if(matches.isEmpty())
			return sequence(record);
		return matches.map(merge(record));
    }

    public static Callable1<Record, Iterable<Record>> leftJoin(final Sequence<Record> records, final Callable1<? super Record, Predicate<Record>> using) {
        return new LeftJoin(records, using);
    }
}