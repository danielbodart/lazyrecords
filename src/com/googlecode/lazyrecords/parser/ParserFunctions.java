package com.googlecode.lazyrecords.parser;

import com.googlecode.funclate.MatchingRenderer;
import com.googlecode.funclate.Renderer;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.collections.PersistentList;

import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.collections.PersistentList.constructors.empty;

public class ParserFunctions {
    private PersistentList<Triple<String, Predicate<?>, Callable1<?, String>>> functions = empty();

    public <T> ParserFunctions add(String name, Predicate<? super T> predicate, Callable1<? super T, String> renderer) {
        functions = functions.append(Triple.<String, Predicate<?>, Callable1<?, String>>triple(name, predicate, renderer));
        return this;
    }

    public <T> ParserFunctions add(String name, Predicate<? super T> predicate, Renderer<? super T> renderer) {
        return add(name, predicate, MatchingRenderer.callable(renderer));
    }

    public <T> Iterable<Triple<String, Predicate<? super T>, Callable1<? super T, String>>> functions() {
        return cast(functions);
    }
}
