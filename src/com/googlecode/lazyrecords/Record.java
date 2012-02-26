package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Unchecked;

import java.util.Map;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Keywords.name;
import static com.googlecode.totallylazy.Callables.asString;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;

public interface Record {
    <T> T get(Keyword<T> keyword);

    <T> Record set(Keyword<T> name, T value);

    Sequence<Pair<Keyword<?>, Object>> fields();

    Sequence<Keyword<?>> keywords();

    Sequence<Object> getValuesFor(Sequence<Keyword<?>> keywords);

    public static final class constructors{
        private constructors(){}

        public static Record record() {
            return new MapRecord();
        }

        public static Record record(final Pair<Keyword<?>, Object>... fields) {
            return Sequences.sequence(fields).fold(record(), functions.updateValues());
        }

        public static Record record(final Sequence<Pair<Keyword<?>, Object>> fields) {
            return fields.fold(record(), functions.updateValues());
        }
    }

    public static final class methods{
        private methods(){}

        public static Keyword<Object> getKeyword(String name, Sequence<Keyword<?>> definitions) {
            return definitions.<Keyword<Object>>unsafeCast().find(where(name(), equalIgnoringCase(name))).getOrElse(keyword(name));
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
            return map(record.fields().map(Callables.<Keyword<?>, Object, String>first(asString(Keyword.class))));
        }
    }

    public static final class functions{
        private functions(){}

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
