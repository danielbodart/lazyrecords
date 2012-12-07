package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.callables.Count;
import com.googlecode.totallylazy.collections.ImmutableMap;
import com.googlecode.totallylazy.collections.ImmutableSortedMap;
import com.googlecode.totallylazy.collections.ListMap;
import com.googlecode.totallylazy.comparators.Maximum;
import com.googlecode.totallylazy.comparators.Minimum;
import com.googlecode.totallylazy.numbers.Integers;
import com.googlecode.totallylazy.numbers.Longs;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.time.Dates;

import java.util.Comparator;
import java.util.Date;

import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.collections.ListMap.listMap;


public class Grammar {
    public static Definition definition(final String name, final Iterable<? extends Keyword<?>> fields) {
        return Definition.constructors.definition(name, fields);
    }

    public static Definition definition(final String name, final Keyword<?> head, final Keyword<?>... tail) {
        return Definition.constructors.definition(name, head, tail);
    }

    public static ImmutableKeyword<Object> keyword(String value) {
        return Keywords.keyword(value);
    }

    public static <T> ImmutableKeyword<T> keyword(String value, Class<? extends T> aClass) {
        return Keywords.keyword(value, aClass);
    }

    public static <T> ImmutableKeyword<T> keyword(Keyword<? extends T> keyword) {
        return Keywords.keyword(keyword);
    }

    public static Record record() {
        return Record.constructors.record();
    }

    public static Record record(final Pair<Keyword<?>, Object>... fields) {
        return Record.constructors.record(fields);
    }

    public static Record record(final Sequence<Pair<Keyword<?>, Object>> fields) {
        return Record.constructors.record(fields);
    }

    public static Function2<Number, Number, Number> average() {
        return Numbers.average();
    }

    public static <T extends Number> Aggregate<T, Number> average(Keyword<T> keyword) {
        return Aggregate.average(keyword);
    }

    public static Maximum.Function<Integer> maximumInteger() {
        return Integers.maximum();
    }

    public static Maximum.Function<Long> maximumLong() {
        return Longs.maximum();
    }

    public static com.googlecode.totallylazy.numbers.Maximum maximumNumber() {
        return Numbers.maximum();
    }

    public static Maximum.Function<Date> maximumDate() {
        return Dates.maximum();
    }

    public static Minimum.Function<Integer> minimumInteger() {
        return Integers.minimum();
    }

    public static Minimum.Function<Long> minimumLong() {
        return Longs.minimum();
    }

    public static com.googlecode.totallylazy.numbers.Minimum minimumNumber() {
        return Numbers.minimum();
    }

    public static Minimum.Function<Date> minimumDate() {
        return Dates.minimum();
    }

    private static final ImmutableMap<Class<?>, Maximum> maximums = ListMap.<Class<?>, Maximum>listMap(Integer.class, maximumInteger(), Long.class, maximumLong(), Number.class, maximumNumber(), Date.class, maximumDate());
    public static <T> Maximum<T> maximum(Class<T> aClass) {
        return cast(maximums.get(aClass).get());
    }

    public static <T extends Comparable<? super T>> Aggregate<T, T> maximum(Keyword<T> keyword) {
        return Aggregate.maximum(keyword);
    }

    private static final ImmutableMap<Class<?>, Minimum> minimums = ListMap.<Class<?>, Minimum>listMap(Integer.class, minimumInteger(), Long.class, minimumLong(), Number.class, minimumNumber(), Date.class, minimumDate());
    public static <T> Minimum<T> minimum(Class<T> aClass) {
        return cast(minimums.get(aClass).get());
    }

    public static <T extends Comparable<? super T>> Aggregate<T, T> minimum(Keyword<T> keyword) {
        return Aggregate.minimum(keyword);
    }

    public static Function2<Number, Number, Number> sum() {
        return Numbers.sum();
    }

    public static <T extends Number> Aggregate<T, Number> sum(Keyword<T> keyword) {
        return Aggregate.sum(keyword);
    }

    public static Aggregates to(final Aggregate<?, ?>... aggregates) {
        return Aggregates.to(aggregates);
    }

    public static Callable1<Record, Iterable<Record>> join(final Sequence<Record> records, final Callable1<? super Record, Predicate<Record>> using) {
        return Join.join(records, using);
    }

    public static Sequence<Pair<Predicate<Record>, Record>> update(final Callable1<? super Record, Predicate<Record>> callable, final Record... records) {
        return Record.methods.update(callable, records);
    }

    public static Sequence<Pair<Predicate<Record>, Record>> update(final Callable1<? super Record, Predicate<Record>> callable, final Sequence<Record> records) {
        return Record.methods.update(callable, records);
    }

    public static Callable1<? super Record, Record> select(final Keyword<?>... keywords) {
        return SelectCallable.select(keywords);
    }

    public static Callable1<? super Record, Record> select(final Sequence<Keyword<?>> keywords) {
        return SelectCallable.select(keywords);
    }

    public static Using using(Keyword<?>... keyword) {
        return Using.using(keyword);
    }

    public static Using using(Sequence<Keyword<?>> keyword) {
        return Using.using(keyword);
    }

    public static <T, R extends Comparable<? super R>> Comparator<T> ascending(final Callable1<? super T, ? extends R> callable) {
        return Callables.ascending(callable);
    }

    public static <T, R extends Comparable<? super R>> Comparator<T> descending(final Callable1<? super T, ? extends R> callable) {
        return Callables.descending(callable);
    }

    public static <T> LogicalPredicate<T> all() {
        return Predicates.all();
    }

    public static <T> LogicalPredicate<T> all(Class<T> aClass) {
        return Predicates.all(aClass);
    }

    public static <T extends Comparable<? super T>> LogicalPredicate<T> between(final T lower, final T upper) {
        return Predicates.between(lower, upper);
    }

    public static <T extends Comparable<? super T>> LogicalPredicate<T> greaterThan(final T comparable) {
        return Predicates.greaterThan(comparable);
    }

    public static <T extends Comparable<? super T>> LogicalPredicate<T> greaterThanOrEqualTo(final T comparable) {
        return Predicates.greaterThanOrEqualTo(comparable);
    }

    public static <T> LogicalPredicate<T> in(final T... values) {
        return Predicates.in(values);
    }

    public static <T> LogicalPredicate<T> in(final Iterable<? extends T> values) {
        return Predicates.in(values);
    }

    public static <T> LogicalPredicate<T> is(final T t) {
        return Predicates.is(t);
    }

    public static <S, T extends Predicate<S>> T is(final T t) {
        return Predicates.is(t);
    }

    public static <T extends Comparable<? super T>> LogicalPredicate<T> lessThan(final T comparable) {
        return Predicates.lessThan(comparable);
    }

    public static <T extends Comparable<? super T>> LogicalPredicate<T> lessThanOrEqualTo(final T comparable) {
        return Predicates.lessThanOrEqualTo(comparable);
    }

    public static <T> LogicalPredicate<T> not(final T t) {
        return Predicates.not(t);
    }

    public static <T> LogicalPredicate<T> not(final Predicate<? super T> t) {
        return Predicates.not(t);
    }

    public static <T> LogicalPredicate<T> notNullValue() {
        return Predicates.notNullValue();
    }

    public static <T> LogicalPredicate<T> notNullValue(final Class<T> aClass) {
        return Predicates.notNullValue(aClass);
    }

    public static <T> LogicalPredicate<T> nullValue() {
        return Predicates.nullValue();
    }

    public static <T> LogicalPredicate<T> nullValue(final Class<T> type) {
        return Predicates.nullValue(type);
    }

    public static <T, R> LogicalPredicate<T> where(final Callable1<? super T, ? extends R> callable, final Predicate<? super R> predicate) {
        return Predicates.where(callable, predicate);
    }

    public static LogicalPredicate<String> contains(final String value) {
        return Strings.contains(value);
    }

    public static LogicalPredicate<String> startsWith(final String value) {
        return Strings.startsWith(value);
    }

    public static LogicalPredicate<String> endsWith(final String value) {
        return Strings.endsWith(value);
    }

    public static Function2<Number, Object, Number> count() {
        return Count.count();
    }


}
