package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.List;

public interface Records {
    Sequence<Record> get(Definition definition);

    Number add(Definition definition, Record... records);

    Number add(Definition definition, Sequence<Record> records);

    Number set(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records);

    Number set(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records);

    Number put(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records);

    Number put(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records);

    Number remove(Definition definition, Predicate<? super Record> predicate);

    Number remove(Definition definition);

    void define(Definition definition);

    boolean exists(Definition definition);

    boolean undefine(Definition definition);
}
