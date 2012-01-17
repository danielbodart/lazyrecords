package com.googlecode.lazyrecords.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;

public interface PredicateParser {
    Predicate<Record> parse(String query, Sequence<? extends Keyword<?>> implicits) throws IllegalArgumentException;
}
