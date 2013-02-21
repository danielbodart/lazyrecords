package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Mapper;

public abstract class RecordTo<R> extends Mapper<Record, R> implements ClientComputation {
    public static <R> RecordTo<R> recordTo(final Callable1<Record, ? extends R> callable) {
        return new RecordTo<R>() {
            @Override
            public R call(Record record) throws Exception {
                return callable.call(record);
            }
        };
    }
}