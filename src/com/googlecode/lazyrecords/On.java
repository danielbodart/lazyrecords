package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Unchecked.cast;

public class On<T> implements Callable1<Record, Predicate<Record>> {
    private final Keyword<T> left,right;
    private final Function1<T, Predicate<T>> predicate;

    protected On(Keyword<T> left, Function1<T, Predicate<T>> predicate, Keyword<T> right) {
        this.left = left;
        this.right = right;
        this.predicate = cast(predicate);
    }

    public static <T> On<T> on(Keyword<T> left, Keyword<T> right) {
        return on(left, On.<T>is(), right);
    }

    public static <T> On<T> on(Keyword<T> left, Callable1<T, ? extends Predicate<T>> predicateCreator, Keyword<T> right) {
        Function1<T, Predicate<T>> function = Functions.function(predicateCreator);
        return new On<T>(left, function, right);
    }

    private static <T> Mapper<T,LogicalPredicate<T>> is() {
        return new Mapper<T, LogicalPredicate<T>>() {
            @Override
            public LogicalPredicate<T> call(T o) throws Exception {
                return Predicates.is(o);
            }
        };
    }

    public Predicate<Record> call(Record record) throws Exception {
        return where(right, predicate.call(left.call(record)));
    }

    public Keyword<T> left() {
        return left;
    }

    public Function1<T, Predicate<T>> predicate() {
        return predicate;
    }

    public Keyword<T> right() {
        return right;
    }
}
