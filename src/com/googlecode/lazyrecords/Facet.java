package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Pair;

import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Callables.returns;

public class Facet<T extends Pair<String, Number>> extends Group<Keyword<?>, T> {

    private Facet(Keyword<?> key, Iterable<T> values) {
        super(key, values);
    }

    public static <T extends Pair<String, Number>> Facet<T> facet(Keyword<?> key, Iterable<T> values) {
        return new Facet<T>(key, values);
    }


    public static class FacetEntry extends Pair<String, Number> {

        public FacetEntry(Callable<? extends String> first, Callable<? extends Number> second) {
            super(first, second);
        }

        public static FacetEntry facetEntry(String name, Number count) {
            return new FacetEntry(returns(name), returns(count));
        }
    }

}
