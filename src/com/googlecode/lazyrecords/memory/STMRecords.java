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
import static com.googlecode.totallylazy.Sequences.reverse;
import static com.googlecode.totallylazy.Sequences.sequence;

public class STMRecords extends AbstractRecords implements Transaction {
    private final StringMappings mappings;
    private final STM stm;
    private STM snapshot;
    private ImmutableList<Callable1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>,
            ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>>> modifications;

    public STMRecords(STM stm, StringMappings mappings) {
        this.stm = stm;
        this.mappings = mappings;
        createSnapshot();
    }

    public STMRecords(STM stm) {
        this(stm, new StringMappings());
    }

    public Sequence<Record> get(Definition definition) {
        return sequence(recordsFor(snapshot.value(), definition)).map(asRecord(definition));
    }

    public Number add(final Definition definition, final Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }

        ImmutableList<ImmutableMap<String, String>> newRecords = records.
                map(asImmutableMap(definition)).
                toImmutableList();
        modify(put(definition, newRecords));

        return newRecords.size();
    }

    public Number remove(final Definition definition, Predicate<? super Record> predicate) {
        ImmutableList<ImmutableMap<String, String>> matches = get(definition).
                filter(predicate).
                <Value<ImmutableMap<String, String>>>unsafeCast().
                map(Callables.<ImmutableMap<String, String>>value()).
                toImmutableList();

        modify(remove(definition, matches));

        return matches.size();
    }

    @Override
    public void commit() {
        stm.modify(applyAll(reverse(modifications)));
    }

    @Override
    public void rollback() {
        createSnapshot();
    }

    private void createSnapshot() {
        snapshot = stm.snapshot();
        modifications = ImmutableList.constructors.empty();
    }

    private void modify(Callable1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> callable) {
        snapshot.modify(callable);
        modifications = modifications.cons(callable);
    }

    private static ImmutableList<ImmutableMap<String, String>> recordsFor(ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> data, Definition definition) {
        return data.get(definition).getOrElse(ImmutableList.constructors.<ImmutableMap<String, String>>empty());
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

    private static <T> Function1<T, T> applyAll(final Iterable<? extends Callable1<T, T>> callables) {
        return new Function1<T, T>() {
            @Override
            public T call(T data) throws Exception {
                return sequence(callables).fold(data, STMRecords.<T>andCall());
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

    private static Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> put(final Definition definition, final ImmutableList<ImmutableMap<String, String>> newRecords) {
        return new Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>>() {
            @Override
            public ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> call(ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> data) throws Exception {
                return data.put(definition, newRecords.joinTo(recordsFor(data, definition)));
            }
        };
    }

    private static Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> remove(final Definition definition, final ImmutableList<ImmutableMap<String, String>> matches) {
        return new Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>>() {
            @Override
            public ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> call(ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> data) throws Exception {
                return data.put(definition, recordsFor(data, definition).removeAll(matches));
            }
        };
    }
}
