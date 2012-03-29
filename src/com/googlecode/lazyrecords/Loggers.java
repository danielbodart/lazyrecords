package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.googlecode.totallylazy.Sequences.sequence;

public final class Loggers implements Logger {
    private final List<Logger> loggers;

    public Loggers() {
        this.loggers = new CopyOnWriteArrayList<Logger>();
    }

    @Override
    public Logger log(Map<String, ?> parameters) {
        sequence(loggers).each(Logger.functions.log(parameters));
        return this;
    }

    public Loggers add(Logger logger){
        loggers.add(logger);
        return this;
    }

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

    public static Callable1<Number, Object> milliseconds(final Map<String, Object> log) {
        return new Callable1<Number, Object>() {
            @Override
            public Object call(Number number) throws Exception {
                return log.put(MILLISECONDS, number);
            }
        };
    }
}
