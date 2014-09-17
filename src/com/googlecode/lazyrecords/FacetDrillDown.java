package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Group;

public class FacetDrillDown extends Group<Keyword<?>, String> {

    private FacetDrillDown(Keyword<?> key, Iterable<? extends String> values) {
        super(key, values);
    }

    public static FacetDrillDown facetDrillDown(Keyword<?> key, Iterable<? extends String> values) {
        return new FacetDrillDown(key, values);
    }
}
