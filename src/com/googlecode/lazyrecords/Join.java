package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Unchecked.cast;

public abstract class Join implements Callable1<Record, Iterable<Record>> {
	protected final Sequence<Record> records;
	protected final Callable1<Record, Predicate<Record>> using;

	public Join(Sequence<Record> records, Callable1<? super Record, Predicate<Record>> using) {
		this.records = records;
		this.using = cast(using);
	}

	public Sequence<Record> records() {
		return records;
	}

	public Callable1<Record, Predicate<Record>> using() {
		return using;
	}

	public static InnerJoin join(Sequence<Record> records,Callable1<Record, Predicate<Record>> using ){
		return InnerJoin.join(records, using);
	}
}
