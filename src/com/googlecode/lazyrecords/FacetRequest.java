package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.returns;

public class FacetRequest extends Pair<Keyword<?>, Integer> {

    protected FacetRequest(Callable<? extends Keyword<?>> first, Callable<? extends Integer> second) {
        super(first, second);
    }

    public static FacetRequest facetRequest(Keyword<?> facetField, Integer maxChildrenCount) {
        return new FacetRequest(returns(facetField), returns(maxChildrenCount));
    }

    public static FacetRequest facetRequest(Keyword<?> facetField) {
        return facetRequest(facetField, Integer.MAX_VALUE);
    }
}
