package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Mapper;

public abstract class FromRecord<R> extends Mapper<Record, R> {
    public static <R> FromRecord<R> fromRecord(final Callable1<Record, ? extends R> callable) {
        return new FromRecord<R>() {
            @Override
            public R call(Record record) throws Exception {
                return callable.call(record);
            }
        };
    }
}