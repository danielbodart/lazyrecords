package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.callables.Count;
import com.googlecode.totallylazy.numbers.Numbers;

import static java.lang.String.format;

public class Aggregate<T, R> extends AliasedKeyword<R> implements Reducer<T, R> {
    private final Reducer<T, R> reducer;

    private Aggregate(final Reducer<T, R> reducer, final Keyword<R> keyword, final String name) {
        super(keyword, name);
        this.reducer = reducer;
    }

    public static <T, R> Aggregate<T, R> aggregate(Reducer<? super T, R> reducer, Keyword<? extends R> keyword, final String name) {
        return new Aggregate<T, R>(Unchecked.<Reducer<T, R>>cast(reducer), Unchecked.<Keyword<R>>cast(keyword), name);
    }

    public static <T, R> Aggregate<T, R> aggregate(Reducer<? super T, R> reducer, Keyword<? extends R> keyword) {
        return aggregate(reducer, keyword, generateName(reducer, keyword));
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

    public Reducer<T, R> reducer() {
        return reducer;
    }

    public Aggregate<T, R> as(Keyword<T> keyword) {
        return as(keyword.name());
    }

    public Aggregate<T, R> as(String name) {
        return aggregate(reducer, source(), name);
    }



    // Factory methods
    public static <T> Aggregate<T, T> maximum(Keyword<T> keyword) {
        return aggregate(Grammar.maximum(keyword.forClass()), keyword);
    }

    public static <T> Aggregate<T, T> minimum(Keyword<T> keyword) {
        return aggregate(Grammar.minimum(keyword.forClass()), keyword);
    }

    public static <T extends Number> Aggregate<T, Number> sum(Keyword<T> keyword) {
        return Aggregate.aggregate(Numbers.sum(), keyword);
    }

    public static <T extends Number> Aggregate<T, Number> average(Keyword<T> keyword) {
        return aggregate(Numbers.average(), numberKeyword(keyword));
    }

    public static Aggregate<Object, Number> count(Keyword<?> keyword) {
        return aggregate(Count.count(), numberKeyword(keyword));
    }

    private static Keyword<Number> numberKeyword(Keyword<?> keyword) {
        return Keywords.keyword(keyword.name(), Number.class);
    }


}
