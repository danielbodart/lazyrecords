package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.callables.Count;
import com.googlecode.totallylazy.numbers.Numbers;

import static java.lang.String.format;

public class Aggregate<T, R> extends AbstractKeyword<R> implements Reducer<T, R> {
    private final Reducer<T, R> reducer;
    private final Keyword<T> source;
    private final String name;
    private final Class<R> rClass;

    private Aggregate(final Reducer<T, R> reducer, final Keyword<T> source, final String name, final Class<R> rClass) {
       this(reducer, source, name, rClass, Record.constructors.record());
    }

    private Aggregate(final Reducer<T, R> reducer, final Keyword<T> source, final String name, final Class<R> rClass, Record metadata) {
        super(metadata);
        this.reducer = reducer;
        this.source = source;
        this.name = name;
        this.rClass = rClass;
    }

    public static <T, R> Aggregate<T, R> aggregate(Reducer<? super T, R> reducer, Keyword<? extends T> keyword, final String name, final Class<R> rClass) {
        return new Aggregate<T, R>(Unchecked.<Reducer<T, R>>cast(reducer), Unchecked.<Keyword<T>>cast(keyword), name, rClass);
    }

    public static <T, R> Aggregate<T, R> aggregate(Reducer<? super T, R> reducer, Keyword<? extends T> keyword, final Class<R> rClass) {
        return aggregate(reducer, keyword, generateName(reducer, keyword), rClass);
    }

    public static <T> Aggregate<T, T> aggregate(Reducer<? super T, T> reducer, Keyword<? extends T> keyword) {
        return aggregate(reducer, keyword, generateName(reducer, keyword), Unchecked.<Class<T>>cast(keyword.forClass()));
    }

    private static String generateName(final Reducer<?, ?> reducer, final Keyword<?> keyword) {
        return format("%s_%s", reducer.getClass().getSimpleName(), replaceIllegalCharacters(keyword.name())).toLowerCase();
    }

    private static String replaceIllegalCharacters(String name) {
        return name.replace("*", "star");
    }

    @Override
    public R call(final R accumulator, final T next) throws Exception {
        return reducer.call(accumulator, next);
    }

    @Override
    public R identity() {
        return reducer.identity();
    }

    @Override
    public Aggregate<T, R> metadata(Record record) {
        return new Aggregate<T, R>(reducer, source, name, rClass, record);
    }

    @Override
    public Class<R> forClass() {
        return rClass;
    }

    @Override
    public String name() {
        return name;
    }

    public Reducer<T, R> reducer() {
        return reducer;
    }

    public Keyword<T> source() {
        return source;
    }

    public Aggregate<T, R> as(Keyword<T> keyword) {
        return as(keyword.name());
    }

    public Aggregate<T, R> as(String name) {
        return aggregate(reducer, source(), name, forClass());
    }


    // Factory methods
    public static <T> Aggregate<T, T> maximum(Keyword<T> keyword) {
        return aggregate(Grammar.maximum(keyword.forClass()), keyword);
    }

    public static <T> Aggregate<T, T> minimum(Keyword<T> keyword) {
        return aggregate(Grammar.minimum(keyword.forClass()), keyword);
    }

    public static <T extends Number> Aggregate<T, Number> sum(Keyword<T> keyword) {
        return aggregate(Numbers.sum(), keyword, Number.class);
    }

    public static <T extends Number> Aggregate<T, Number> average(Keyword<T> keyword) {
        return aggregate(Numbers.average(), keyword, Number.class);
    }

    public static Aggregate<Object, Number> count(Keyword<?> keyword) {
        return aggregate(Count.count(), keyword, Number.class);
    }

}
