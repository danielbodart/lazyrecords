package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.collections.PersistentList;

import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.collections.PersistentList.constructors.list;

public final class Loggers implements Logger {
    private final PersistentList<Logger> loggers;

    private Loggers(final PersistentList<Logger> empty) {
        this.loggers = empty;
    }

    public static Loggers loggers() {
        return loggers(PersistentList.constructors.<Logger>empty());
    }

    public static Loggers loggers(final Logger... loggers) {
        return new Loggers(list(loggers));
    }

    public static Loggers loggers(final PersistentList<Logger> empty) {
        return new Loggers(empty);
    }

    @Override
    public Logger log(Map<String, ?> parameters) {
        sequence(loggers).each(Logger.functions.log(parameters));
        return this;
    }

    public Loggers add(Logger logger) {
        return loggers(loggers.cons(logger));
    }

    public static final String TYPE = "type";
    public static final String EXPRESSION = "expression";
    public static final String MILLISECONDS = "milliseconds";
    public static final String ROWS = "rows";
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
