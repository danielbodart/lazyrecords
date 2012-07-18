package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.Transaction;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.collections.ImmutableList;
import com.googlecode.totallylazy.collections.ImmutableMap;
import com.googlecode.totallylazy.collections.ImmutableSortedMap;

import static com.googlecode.lazyrecords.Definition.functions.sortFields;
import static com.googlecode.totallylazy.Sequences.sequence;

public class TransactionalMemoryRecords extends AbstractRecords implements Transaction {
    private final StringMappings mappings;
    private final TransactionalMemory transactionalMemory;
    private TransactionalMemory snapShot;
    private ImmutableList<Callable1<ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>,
            ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>>> modifications = ImmutableList.constructors.empty();

    public TransactionalMemoryRecords(TransactionalMemory transactionalMemory, StringMappings mappings) {
        this.transactionalMemory = transactionalMemory;
        this.mappings = mappings;
        snapShot = transactionalMemory.snapShot();
    }

    public TransactionalMemoryRecords(TransactionalMemory transactionalMemory) {
        this(transactionalMemory, new StringMappings());
    }

    public Sequence<Record> get(Definition definition) {
        return sequence(recordsFor(snapShot.value(), definition)).map(asRecord(definition));
    }

    public Number add(final Definition definition, final Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }

        final ImmutableList<ImmutableMap<String, String>> newRecords = records.map(sortFields(definition)).map(asImmutableMap(definition)).toImmutableList();
        snapShot.modify(put(definition, newRecords));
        modifications = modifications.cons(put(definition, newRecords));

        return newRecords.size();
    }

    private static ImmutableList<ImmutableMap<String, String>> recordsFor(ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>> data, Definition definition) {
        return data.get(definition).getOrElse(ImmutableList.constructors.<ImmutableMap<String, String>>empty());
    }

    private static Function1<ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>> put(final Definition definition, final ImmutableList<ImmutableMap<String, String>> newRecords) {
        return new Function1<ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>>() {
            @Override
            public ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>> call(ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>> data) throws Exception {
                return data.put(definition, newRecords.joinTo(recordsFor(data, definition)));
            }
        };
    }

    public Number remove(final Definition definition, Predicate<? super Record> predicate) {
        final ImmutableList<ImmutableMap<String, String>> matches = get(definition).
                filter(predicate).
                <Value<ImmutableMap<String, String>>>unsafeCast().
                map(Callables.<ImmutableMap<String, String>>value()).
                toImmutableList();

        snapShot.modify(remove(definition, matches));
        modifications = modifications.cons(remove(definition, matches));

        return matches.size();
    }

    private static Function1<ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>> remove(final Definition definition, final ImmutableList<ImmutableMap<String, String>> matches) {
        return new Function1<ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>>>() {
            @Override
            public ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>> call(ImmutableSortedMap<Definition, ImmutableList<ImmutableMap<String, String>>> data) throws Exception {
                return data.put(definition, recordsFor(data, definition).removeAll(matches));
            }
        };
    }

    private Callable1<ImmutableMap<String, String>, Record> asRecord(final Definition definition) {
        return new Callable1<ImmutableMap<String, String>, Record>() {
            @Override
            public Record call(ImmutableMap<String, String> data) throws Exception {
                return SourceRecord.record(data, definition.fields().map(values(data)));
            }
        };
    }

    private Callable1<Keyword<?>, Pair<Keyword<?>, Object>> values(final ImmutableMap<String, String> map) {
        return new Callable1<Keyword<?>, Pair<Keyword<?>, Object>>() {
            @Override
            public Pair<Keyword<?>, Object> call(Keyword<?> keyword) throws Exception {
                return Pair.<Keyword<?>, Object>pair(keyword, mappings.toValue(keyword.forClass(), map.get(keyword.name()).getOrNull()));
            }
        };
    }

    private Callable1<Record, ImmutableMap<String, String>> asImmutableMap(final Definition definition) {
        return new Callable1<Record, ImmutableMap<String, String>>() {
            @Override
            public ImmutableMap<String, String> call(Record record) throws Exception {
                return ImmutableSortedMap.constructors.sortedMap(definition.fields().map(values(record)));
            }
        };
    }

    private Callable1<Keyword<?>, Pair<String, String>> values(final Record record) {
        return new Callable1<Keyword<?>, Pair<String, String>>() {
            @Override
            public Pair<String, String> call(Keyword<?> keyword) throws Exception {
                return Pair.pair(keyword.name(), mappings.toString(keyword.forClass(), record.get(keyword)));
            }
        };
    }

    @Override
    public void commit() {
        transactionalMemory.modify(callAll(modifications));
    }

    private static <T> Function1<T, T> callAll(final Iterable<Callable1<T, T>> callables) {
        return new Function1<T, T>() {
            @Override
            public T call(T data) throws Exception {
                return sequence(callables).fold(data, TransactionalMemoryRecords.<T>andCall());
            }
        };
    }

    private static <T> Function2<T, Callable1<T, T>, T> andCall() {
        return new Function2<T, Callable1<T, T>, T>() {
            @Override
            public T call(T data, Callable1<T, T> callable) throws Exception {
                return callable.call(data);
            }
        };
    }

    @Override
    public void rollback() {
        snapShot = transactionalMemory.snapShot();
    }
}
