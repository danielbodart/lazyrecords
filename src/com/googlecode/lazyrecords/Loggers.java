package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;

import java.util.Map;

public final class Loggers {
    public static final String TYPE = "type";
    public static final String EXPRESSION = "expression";
    public static final String MILLISECONDS = "milliseconds";
    public static final String COUNT = "count";
    public static final String MESSAGE = "message";
    public static final String COMPARATOR = "comparator";
    public static final String PREDICATE = "predicate";
    public static final String FUNCTION = "function";


    public static final String SQL = "Sql";
    public static final String LUCENE = "Lucene";
    public static final String SIMPLE_DB = "SimpleDb";

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
