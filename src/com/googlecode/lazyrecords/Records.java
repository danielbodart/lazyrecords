package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.List;

public interface Records {
    Sequence<Record> get(RecordName recordName);

    void define(RecordName recordName, Keyword<?>... fields);

    boolean exists(RecordName recordName);

    Number add(RecordName recordName, Record... records);

    Number add(RecordName recordName, Sequence<Record> records);

    Number set(RecordName recordName, Pair<? extends Predicate<? super Record>, Record>... records);

    Number set(RecordName recordName, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records);

    Number put(RecordName recordName, Pair<? extends Predicate<? super Record>, Record>... records);

    Number put(RecordName recordName, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records);

    Number remove(RecordName recordName, Predicate<? super Record> predicate);

    Number remove(RecordName recordName);

    List<Keyword<?>> undefine(RecordName recordName);
}
