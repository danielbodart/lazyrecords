package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.predicates.EqualsBinaryPredicate;
import com.googlecode.totallylazy.predicates.LogicalBinaryPredicate;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import static com.googlecode.totallylazy.LazyException.lazyException;
import static com.googlecode.totallylazy.Predicates.where;

public class On<T> implements Joiner {
    private final Keyword<T> left, right;
    private final LogicalBinaryPredicate<T> predicate;

    protected On(Keyword<T> left, LogicalBinaryPredicate<T> predicate, Keyword<T> right) {
        this.left = left;
        this.right = right;
        this.predicate = predicate;
    }

    public static <T> On<T> on(Keyword<T> left, Keyword<T> right) {
        return on(left, EqualsBinaryPredicate.<T>is(), right);
    }

    public static <T> On<T> on(Keyword<T> left, LogicalBinaryPredicate<T> predicate, Keyword<T> right) {
        return new On<T>(left, predicate, right);
    }

    public LogicalPredicate<Record> call(Record record) throws Exception {
        return where(right, predicate.apply(left.call(record)));
    }

    public Keyword<T> left() {
        return left;
    }

    public LogicalBinaryPredicate<T> predicate() {
        return predicate;
    }

    public Keyword<T> right() {
        return right;
    }

    @Override
    public boolean matches(final Record a, final Record b) {
        try {
            return call(a).matches(b);
        } catch (Exception e) {
            throw lazyException(e);
        }
    }
}
