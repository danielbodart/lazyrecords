package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.callables.Count;
import com.googlecode.totallylazy.comparators.Maximum;
import com.googlecode.totallylazy.comparators.Minimum;
import com.googlecode.totallylazy.numbers.Average;
import com.googlecode.totallylazy.numbers.Sum;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.where;
import static java.lang.String.format;

public class SetFunctionType extends TextOnlyExpression implements ValueExpression {
    @SuppressWarnings("unchecked")
    private static final Map<Class<? extends Reducer>, String> names = new LinkedHashMap<Class<? extends Reducer>, String>() {{
        put(Count.class, "count");
        put(Average.class, "avg");
        put(Sum.class, "sum");
        put(Minimum.class, "min");
        put(Maximum.class, "max");
    }};

    public SetFunctionType(Reducer<?, ?> reducer, Keyword<?> column) {
        super(functionName(reducer.getClass(), column));
    }

    public static String functionName(final Class<? extends Reducer> aClass, Keyword<?> column) {
        return format("%s(%s)", get(aClass), name(column));
    }

    private static String get(Class<? extends Reducer> aClass) {
        return pairs(names).
                find(where(Callables.<Class<? extends Reducer>>first(), classAssignableFrom(aClass))).
                map(second(String.class)).
                getOrThrow(new UnsupportedOperationException());
    }

    public static SetFunctionType setFunctionType(Reducer<?, ?> reducer, Keyword<?> column) {
        return new SetFunctionType(reducer, column);
    }

    public static LogicalPredicate<Class<?>> classAssignableFrom(final Class<?> aClass) {
        return new LogicalPredicate<Class<?>>() {
            @Override
            public boolean matches(Class<?> other) {
                return other.isAssignableFrom(aClass);
            }
        };
    }
}
