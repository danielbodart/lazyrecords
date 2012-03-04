package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;

import java.util.Map;

public final class Loggers {
    public static final String MILLISECONDS = "milliseconds";
    public static final String SQL = "sql";
    public static final String COUNT = "count";
    public static final String LUCENE = "lucene";

    private Loggers() {}

    public static Callable1<Number, Object> milliseconds(final Map<String, Object> log) {
        return new Callable1<Number, Object>() {
            @Override
            public Object call(Number number) throws Exception {
                return log.put(MILLISECONDS, number);
            }
        };
    }
}
