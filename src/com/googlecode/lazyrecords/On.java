package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;

import static com.googlecode.totallylazy.Predicates.*;

public class On implements Callable1<Record, Predicate<Record>> {
    private final Keyword<?> left,right;

    protected On(Keyword<?> left, Keyword<?> right) {
        this.left = left;
        this.right = right;
    }

    public static On on(Keyword<?> left, Keyword<?> right) {
        return new On(left, right);
    }

    public Predicate<Record> call(Record record) {
        return where(right, is(record.get(left)));
    }

    public Keyword<?> left() {
        return left;
    }

    public Keyword<?> right() {
        return right;
    }
}
