package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Mapper;

public abstract class ToRecord<T> extends Mapper<T, Record> {
    public static <T> ToRecord<T> toRecord(final Callable1<? super T, Record> callable) {
        return new ToRecord<T>() {
            @Override
            public Record call(T t) throws Exception {
                return callable.call(t);
            }
        };
    }
}
