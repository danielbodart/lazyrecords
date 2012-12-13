package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Unchecked;

import java.util.Map;

import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.collections.ListMap.listMap;

public interface Record {
    <T> T get(Keyword<T> keyword);

    <T> Option<T> getOption(Keyword<T> keyword);

    <T> Record set(Keyword<T> name, T value);

    Sequence<Pair<Keyword<?>, Object>> fields();

    Sequence<Keyword<?>> keywords();

    Sequence<Object> getValuesFor(Sequence<Keyword<?>> keywords);

    public static final class constructors {
        private constructors() {
        }

        public static Record record() {
            return new ImmutableMapRecord();
        }

        public static <A> Record record(Keyword<A> aKeyword, A a) {
            return record().set(aKeyword, a);
        }

        public static <A, B> Record record(Keyword<A> aKeyword, A a, Keyword<B> bKeyword, B b) {
            return record().set(aKeyword, a).set(bKeyword, b);
        }

        public static <A, B, C> Record record(Keyword<A> aKeyword, A a, Keyword<B> bKeyword, B b, Keyword<C> cKeyword, C c) {
            return record().set(aKeyword, a).set(bKeyword, b).set(cKeyword, c);
        }

        public static <A, B, C, D> Record record(Keyword<A> aKeyword, A a, Keyword<B> bKeyword, B b, Keyword<C> cKeyword, C c, Keyword<D> dKeyword, D d) {
            return record().set(aKeyword, a).set(bKeyword, b).set(cKeyword, c).set(dKeyword, d);
        }

        public static <A, B, C, D, E> Record record(Keyword<A> aKeyword, A a, Keyword<B> bKeyword, B b, Keyword<C> cKeyword, C c, Keyword<D> dKeyword, D d, Keyword<E> eKeyword, E e) {
            return record().set(aKeyword, a).set(bKeyword, b).set(cKeyword, c).set(dKeyword, d).set(eKeyword, e);
        }

        public static Record record(final Pair<Keyword<?>, Object>... fields) {
            return record(sequence(fields));
        }

        public static Record record(final Iterable<? extends Pair<Keyword<?>, Object>> fields) {
            return new ImmutableMapRecord(listMap(fields));
        }
    }

    public static final class methods {
        private methods() {
        }

        @Deprecated // please use Keywords.matchKeyword);
        public static Keyword<Object> getKeyword(String name, Sequence<? extends Keyword<?>> definitions) {
            return Keywords.matchKeyword(name, definitions);
        }

        public static Record filter(Record original, Keyword<?>... fields) {
            return filter(original, Sequences.sequence(fields));
        }

        public static Record filter(Record original, Sequence<Keyword<?>> fields) {
            return constructors.record(original.fields().filter(where(Callables.<Keyword<?>>first(), is(in(fields)))));
        }

        public static Sequence<Pair<Predicate<Record>, Record>> update(final Callable1<? super Record, Predicate<Record>> callable, final Record... records) {
            Sequence<Record> sequence = sequence(records);
            return update(callable, sequence);
        }

        public static Sequence<Pair<Predicate<Record>, Record>> update(final Callable1<? super Record, Predicate<Record>> callable, final Sequence<Record> records) {
            return records.map(functions.toPair(callable));
        }

        public static Map<String, Object> toMap(Record record) {
            return map(record.fields().map(Callables.<Keyword<?>, Object, String>first(asString())));
        }
    }

    public static final class functions {
        private functions() {
        }

        public static Function2<Record, Pair<Keyword<?>, Object>, Record> updateValues() {
            return new Function2<Record, Pair<Keyword<?>, Object>, Record>() {
                public Record call(Record record, Pair<Keyword<?>, Object> field) throws Exception {
                    return record.set(Unchecked.<Keyword<Object>>cast(field.first()), field.second());
                }
            };
        }

        public static Function1<Record, Record> merge(final Record other) {
            return merge(other.fields());
        }

        public static Function1<Record, Record> merge(final Sequence<Pair<Keyword<?>, Object>> fields) {
            return new Function1<Record, Record>() {
                public Record call(Record record) throws Exception {
                    return fields.fold(record, updateValues());
                }
            };
        }

        public static Function1<Record, Sequence<Object>> getValuesFor(final Sequence<Keyword<?>> fields) {
            return new Function1<Record, Sequence<Object>>() {
                public Sequence<Object> call(Record record) throws Exception {
                    return record.getValuesFor(fields);
                }
            };
        }

        public static Function1<Record, Pair<Predicate<Record>, Record>> toPair(final Callable1<? super Record, Predicate<Record>> callable) {
            return new Function1<Record, Pair<Predicate<Record>, Record>>() {
                public Pair<Predicate<Record>, Record> call(Record record) throws Exception {
                    return Pair.pair(callable.call(record), record);
                }
            };
        }

        public static Function1<Record, Map<String, Object>> asMap() {
            return new Function1<Record, Map<String, Object>>() {
                public Map<String, Object> call(Record record) throws Exception {
                    return methods.toMap(record);
                }
            };
        }

        public static Function2<Map<String, Object>, Pair<Keyword<?>, Object>, Map<String, Object>> intoMap() {
            return new Function2<Map<String, Object>, Pair<Keyword<?>, Object>, Map<String, Object>>() {
                public Map<String, Object> call(Map<String, Object> map, Pair<Keyword<?>, Object> pair) throws Exception {
                    map.put(pair.first().toString(), pair.second());
                    return map;
                }
            };
        }
    }
}
