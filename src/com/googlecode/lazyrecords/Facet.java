package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Pair;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.returns;

public class Facet<T extends Pair<Object, Number>> extends Group<Keyword<?>, T> {

    private Facet(Keyword<?> key, Iterable<T> values) {
        super(key, values);
    }

    public static <T extends Pair<Object, Number>> Facet<T> facet(Keyword<?> key, Iterable<T> values) {
        return new Facet<T>(key, values);
    }


    public static class FacetEntry extends Pair<Object, Number> {

        public FacetEntry(Callable<?> first, Callable<? extends Number> second) {
            super(first, second);
        }

        public static FacetEntry facetEntry(Object name, Number count) {
            return new FacetEntry(returns(name), returns(count));
        }
    }

}
