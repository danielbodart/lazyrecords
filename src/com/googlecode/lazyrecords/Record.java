package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.collections.PersistentMap;

import java.util.Map;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.collections.ListMap.listMap;

public interface Record {
    <T> T get(Keyword<T> keyword);

    <T> Option<T> getOption(Keyword<T> keyword);

    <T> Record set(Keyword<T> name, T value);

    Sequence<Pair<Keyword<?>, Object>> fields();

    Sequence<Keyword<?>> keywords();

    <T> Sequence<T> valuesFor(Sequence<? extends Keyword<? extends T>> keywords);

    public static final class constructors {
        public static Record record() {
            return new PersistentRecord();
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

        public static Record record(final Iterable<? extends Pair<? extends Keyword<?>, ?>> fields) {
            return new PersistentRecord(listMap(Unchecked.<Iterable<? extends Pair<Keyword<?>, Object>>>cast(fields)));
        }
    }

    public static final class methods {
        /** @deprecated Replaced by {@link Keyword.methods#matchKeyword(String, Sequence)}  } */
        @Deprecated
        public static Keyword<Object> getKeyword(String name, Sequence<? extends Keyword<?>> definitions) {
            return Keyword.methods.matchKeyword(name, definitions);
        }

        public static Record filter(Record original, Keyword<?>... fields) {
            return filter(original, Sequences.sequence(fields));
        }

        public static Record merge(Record first, Record second, Record... others) {
            return merge(sequence(first, second).join(sequence(others)));
        }

        public static Record merge(Iterable<Record> records) {
            return sequence(records).reduce(functions.merge());
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
        public static Function2<Record, Pair<Keyword<?>, Object>, Record> updateValues() {
            return new Function2<Record, Pair<Keyword<?>, Object>, Record>() {
                public Record call(Record record, Pair<Keyword<?>, Object> field) throws Exception {
                    return record.set(Unchecked.<Keyword<Object>>cast(field.first()), field.second());
                }
            };
        }

        public static Reducer<Record, Record> merge() {
            return new Reducer<Record, Record>() {
                @Override
                public Record call(Record first, Record second) throws Exception {
                    return second.fields().fold(first, updateValues());
                }

                @Override
                public Record identity() {
                    return record();
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

        public static <T> RecordTo<T> get(final Keyword<T> keyword) {
            return new RecordTo<T>() {
                @Override
                public T call(Record record) throws Exception {
                    return record.get(keyword);
                }
            };
        }

        public static <T> RecordTo<Option<T>> getOption(final Keyword<T> keyword) {
            return new RecordTo<Option<T>>() {
                public Option<T> call(Record record) throws Exception {
                    return record.getOption(keyword);
                }
            };
        }

        public static Function1<Keyword<?>, Object> getFrom(final Record record) {
            return new Function1<Keyword<?>, Object>() {
                public Object call(Keyword<?> keyword) throws Exception {
                    return record.get(keyword);
                }
            };
        }

        public static <T> RecordTo<Sequence<T>> getValuesFor(final Sequence<? extends Keyword<? extends T>> fields) {
            return new RecordTo<Sequence<T>>() {
                public Sequence<T> call(Record record) throws Exception {
                    return record.valuesFor(fields);
                }
            };
        }

        public static RecordTo<Pair<Predicate<Record>, Record>> toPair(final Callable1<? super Record, Predicate<Record>> callable) {
            return new RecordTo<Pair<Predicate<Record>, Record>>() {
                public Pair<Predicate<Record>, Record> call(Record record) throws Exception {
                    return Pair.pair(callable.call(record), record);
                }
            };
        }

        public static RecordTo<Map<String, Object>> asMap() {
            return new RecordTo<Map<String, Object>>() {
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

        public static RecordTo<Sequence<Keyword<?>>> keywords = new RecordTo<Sequence<Keyword<?>>>() {
            public Sequence<Keyword<?>> call(Record record) throws Exception {
                return record.keywords();
            }
        };

        public static RecordTo<Sequence<Keyword<?>>> keywords() {
            return keywords;
        }

        public static <T> Mapper<T, Record> set(final Record record, final Keyword<T> keyword) {
            return new Mapper<T, Record>() {
                @Override
                public Record call(T value) throws Exception {
                    return record.set(keyword, value);
                }
            };
        }
    }
}
