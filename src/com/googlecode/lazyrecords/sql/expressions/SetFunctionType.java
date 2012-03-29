package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.callables.CountNotNull;
import com.googlecode.totallylazy.numbers.Average;
import com.googlecode.totallylazy.numbers.Sum;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Maximum;
import com.googlecode.lazyrecords.Minimum;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static java.lang.String.format;

public class SetFunctionType extends TextOnlyExpression {
    private static final Map<Class<?>, String> names = new HashMap<Class<?>, String>() {{
        put(CountNotNull.class, "count");
        put(Average.class, "avg");
        put(Sum.class, "sum");
        put(Minimum.class, "min");
        put(Maximum.class, "max");
    }};

    public SetFunctionType(Callable2<?, ?, ?> callable, Keyword<?> column) {
        super(functionName(callable.getClass(), column));
    }

    public static String functionName(final Class<? extends Callable2> aClass, Keyword<?> column) {
        if (!names.containsKey(aClass)) {
            throw new UnsupportedOperationException();
        }
        return format("%s(%s)", names.get(aClass), name(column));
    }

    public static SetFunctionType setFunctionType(Callable2<?, ?, ?> callable, Keyword<?> column) {
        return new SetFunctionType(callable, column);
    }
}
