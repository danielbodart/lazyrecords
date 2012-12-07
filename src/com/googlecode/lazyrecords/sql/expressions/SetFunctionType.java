package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.callables.Count;
import com.googlecode.totallylazy.comparators.Maximum;
import com.googlecode.totallylazy.comparators.Minimum;
import com.googlecode.totallylazy.numbers.Average;
import com.googlecode.totallylazy.numbers.Sum;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.classAssignableTo;
import static com.googlecode.totallylazy.Predicates.where;
import static java.lang.String.format;

public class SetFunctionType extends TextOnlyExpression {
    private static final Map<Class<?>, String> names = new LinkedHashMap<Class<?>, String>() {{
        put(Count.class, "count");
        put(Average.class, "avg");
        put(Sum.class, "sum");
        put(Minimum.class, "min");
        put(Maximum.class, "max");
    }};

    public SetFunctionType(Callable2<?, ?, ?> callable, Keyword<?> column) {
        super(functionName(callable.getClass(), column));
    }

    public static String functionName(final Class<? extends Callable2> aClass, Keyword<?> column) {
        return format("%s(%s)", get(aClass), name(column));
    }

    private static String get(Class<? extends Callable2> aClass) {
        return pairs(names).
                find(where(Callables.<Class<?>>first(), classAssignableFrom(aClass))).
                map(second(String.class)).
                getOrThrow(new UnsupportedOperationException());
    }

    public static SetFunctionType setFunctionType(Callable2<?, ?, ?> callable, Keyword<?> column) {
        return new SetFunctionType(callable, column);
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
