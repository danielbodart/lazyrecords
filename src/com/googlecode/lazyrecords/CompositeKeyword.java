package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.proxy.Generics;

import static com.googlecode.lazyrecords.Record.constructors.record;

public class CompositeKeyword<T> extends AbstractKeyword<T> implements Aliased {
    private final Callable2<T, T, T> combiner;
    private final Sequence<Keyword<T>> keywords;

    private CompositeKeyword(Record metadata, Callable2<T, T, T> combiner, Sequence<Keyword<T>> keywords) {
        super(metadata);
        this.combiner = combiner;
        this.keywords = keywords;
    }

    public static <T> CompositeKeyword<T> compose(Record metadata, Callable2<? super T, ? super T, ? extends T> combiner, Sequence<? extends Keyword<T>> keywords) {
        return new CompositeKeyword<T>(metadata, Unchecked.<Callable2<T, T, T>>cast(combiner), keywords.<Keyword<T>>unsafeCast());
    }

    public static <T> CompositeKeyword<T> compose(Callable2<? super T, ? super T, ? extends T> combiner, Sequence<? extends Keyword<T>> keywords) {
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
        return combiner.toString() + "_" + keywords.toString("_");
    }

    public Callable2<T, T, T> combiner() {
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
