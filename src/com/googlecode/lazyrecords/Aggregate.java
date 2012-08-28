package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.callables.Count;
import com.googlecode.totallylazy.comparators.Maximum;
import com.googlecode.totallylazy.comparators.Minimum;
import com.googlecode.totallylazy.numbers.Numbers;

import static com.googlecode.totallylazy.Unchecked.cast;
import static java.lang.String.format;

public class Aggregate<T, R> extends AliasedKeyword<T> implements Callable2<T, T, R> {
    private final Callable2<? super T, ? super T, R> callable;

    public Aggregate(final Callable2<? super T, ? super T, R> callable, final Keyword<T> keyword, final String name) {
        super(keyword, name);
        this.callable = callable;
    }

    public Aggregate(final Callable2<? super T, ? super T, R> callable, final Keyword<T> keyword) {
        this(callable, keyword, generateName(callable, keyword));
    }

    private static <T, R> String generateName(final Callable2<? super T, ? super T, R> callable, final Keyword<?> keyword) {
        return format("%s_%s", callable.getClass().getSimpleName(), replaceIllegalCharacters(keyword.name())).toLowerCase();
    }

    private static String replaceIllegalCharacters(String name) {
        return name.replace("*", "star");
    }

    public R call(final T accumulator, final T next) throws Exception {
        return callable.call(accumulator, next);
    }

    public Callable2<? super T, ? super T, R> callable() {
        return callable;
    }

    public static <T, R> Aggregate<T, R> aggregate(Callable2<? super T, ? super T, R> callable, Keyword<?> keyword) {
        return new Aggregate<T, R>(callable, Unchecked.<Keyword<T>>cast(keyword));
    }

    public static <T extends Comparable<? super T>> Aggregate<T, T> maximum(Keyword<T> keyword) {
        return aggregate(Maximum.<T>maximum(), keyword);
    }

    public static <T extends Comparable<? super T>> Aggregate<T, T> minimum(Keyword<T> keyword) {
        return aggregate(Minimum.<T>minimum(), keyword);
    }

    public static <T extends Number> Aggregate<T, Number> sum(Keyword<T> keyword) {
        return aggregate(Numbers.sum(), keyword);
    }

    public static <T extends Number> Aggregate<T, Number> average(Keyword<T> keyword) {
        return aggregate(Numbers.average(), keyword);
    }

    public static Aggregate<Number, Number> count(Keyword<?> keyword) {
        return aggregate(Count.count(), keyword);
    }

    public Aggregate<T, R> as(Keyword<T> keyword) {
        return as(keyword.name());
    }

    public Aggregate<T, R> as(String name) {
        return new Aggregate<T, R>(callable, source(), name);
    }
}
