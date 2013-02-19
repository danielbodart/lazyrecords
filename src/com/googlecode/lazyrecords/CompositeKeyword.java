package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.proxy.Generics;

import static com.googlecode.lazyrecords.Record.constructors.record;

public class CompositeKeyword<T> extends AbstractKeyword<T> implements Aliased {
    private final Binary<T> combiner;
    private final Sequence<Keyword<T>> keywords;

    private CompositeKeyword(Record metadata, Binary<T> combiner, Sequence<Keyword<T>> keywords) {
        super(metadata);
        this.combiner = combiner;
        this.keywords = keywords;
    }

    public static <T> CompositeKeyword<T> compose(Record metadata, Binary<T> combiner, Sequence<? extends Keyword<T>> keywords) {
        return new CompositeKeyword<T>(metadata, combiner, keywords.<Keyword<T>>unsafeCast());
    }

    public static <T> CompositeKeyword<T> compose(Binary<T> combiner, Sequence<? extends Keyword<T>> keywords) {
        return compose(record(), combiner, keywords);
    }

    @Override
    public CompositeKeyword<T> metadata(Record metadata) {
        return new CompositeKeyword<T>(metadata, combiner, keywords);
    }

    @Override
    public Class<T> forClass() {
        return Generics.getGenericSuperclassType(combiner.getClass(), 0);
    }

    @Override
    public String name() {
        return combiner.toString() + "_" + keywords.map(functions.name).toString("_");
    }

    public Binary<T> combiner() {
        return combiner;
    }

    public Sequence<Keyword<T>> keywords() {
        return keywords;
    }

    @Override
    public T call(Record record) throws Exception {
        return record.valuesFor(keywords).reduce(combiner);
    }
}
