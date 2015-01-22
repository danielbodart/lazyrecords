package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Value;

public class FieldBasedFacetingPolicy implements Value<Predicate<String>> {

    private final Predicate<String> value;

    public FieldBasedFacetingPolicy(Predicate<String> value) {
        this.value = value;
    }

    @Override
    public Predicate<String> value() {
        return value;
    }
}
