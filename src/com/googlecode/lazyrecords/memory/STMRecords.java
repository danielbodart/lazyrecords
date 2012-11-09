package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.Transaction;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.collections.ImmutableList;
import com.googlecode.totallylazy.collections.ImmutableMap;
import com.googlecode.totallylazy.collections.ImmutableSortedMap;

import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.lazyrecords.Record.methods.filter;
import static com.googlecode.totallylazy.Sequences.reverse;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class STMRecords extends AbstractRecords implements Transaction {
    private final StringMappings mappings;
    private final STM stm;
    private STM snapshot;
    private ImmutableList<Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>,
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
        return sequence(listFor(snapshot.value(), definition)).map(asRecord(definition));
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
        ImmutableList<ImmutableMap<String, String>> matches = matches(predicate, get(definition));

        modify(removeAll(definition, predicate));

        return matches.size();
    }


    private static ImmutableList<ImmutableMap<String, String>> matches(Predicate<? super Record> predicate, Sequence<Record> records) {
        return records.filter(predicate).
                    <Value<ImmutableMap<String, String>>>unsafeCast().
                    map(Callables.<ImmutableMap<String, String>>value()).
                    toImmutableList();
    }

    @Override
    public Number set(final Definition definition, final Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        modify(new Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>>() {
            @Override
            public ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> call(ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> data) throws Exception {
                return data.put(definition, listFor(data, definition).map(new Function1<ImmutableMap<String, String>, ImmutableMap<String, String>>() {
                    @Override
                    public ImmutableMap<String, String> call(ImmutableMap<String, String> row) throws Exception {
                        return records.fold(row, new Function2<ImmutableMap<String, String>, Pair<? extends Predicate<? super Record>, Record>, ImmutableMap<String, String>>() {
                            @Override
                            public ImmutableMap<String, String> call(ImmutableMap<String, String> fields, Pair<? extends Predicate<? super Record>, Record> pair) throws Exception {
                                Record original = asRecord(definition, fields);
                                if (pair.first().matches(original)) return asImmutableMap(definition, merge(filter(pair.second(), definition.fields())).call(original));
                                return fields;
                            }
                        });
                    }
                }));
            }
        });
        return super.set(definition, records);
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

    private void modify(Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> callable) {
        snapshot.modify(callable);
        modifications = modifications.cons(callable);
    }

    private static <M extends ImmutableMap<String, String>> ImmutableList<M> listFor(ImmutableMap<Definition, ImmutableList<M>> data, Definition definition) {
        return data.get(definition).getOrElse(ImmutableList.constructors.<M>empty());
    }

    private Function1<ImmutableMap<String, String>, Record> asRecord(final Definition definition) {
        return new Function1<ImmutableMap<String, String>, Record>() {
            @Override
            public Record call(ImmutableMap<String, String> data) throws Exception {
                return asRecord(definition, data);
            }
        };
    }

    private Record asRecord(Definition definition, ImmutableMap<String, String> data) {
        return SourceRecord.record(data, definition.fields().map(values(data)));
    }

    private Function1<Keyword<?>, Pair<Keyword<?>, Object>> values(final ImmutableMap<String, String> map) {
        return new Function1<Keyword<?>, Pair<Keyword<?>, Object>>() {
            @Override
            public Pair<Keyword<?>, Object> call(Keyword<?> keyword) throws Exception {
                return Pair.<Keyword<?>, Object>pair(keyword, mappings.toValue(keyword.forClass(), map.get(keyword.name()).getOrNull()));
            }
        };
    }

    private Function1<Record, ImmutableMap<String, String>> asImmutableMap(final Definition definition) {
        return new Function1<Record, ImmutableMap<String, String>>() {
            @Override
            public ImmutableMap<String, String> call(Record record) throws Exception {
                return STMRecords.this.asImmutableMap(definition, record);
            }
        };
    }

    private ImmutableMap<String, String> asImmutableMap(Definition definition, Record record) {
        return ImmutableSortedMap.constructors.sortedMap(definition.fields().map(values(record)));
    }

    private Function1<Keyword<?>, Pair<String, String>> values(final Record record) {
        return new Function1<Keyword<?>, Pair<String, String>>() {
            @Override
            public Pair<String, String> call(Keyword<?> keyword) throws Exception {
                return Pair.pair(keyword.name(), mappings.toString(keyword.forClass(), record.get(keyword)));
            }
        };
    }

    private static <T> Function1<T, T> applyAll(final Iterable<? extends Function1<T, T>> callables) {
        return new Function1<T, T>() {
            @Override
            public T call(T data) throws Exception {
                return sequence(callables).fold(data, STMRecords.<T>andCall());
            }
        };
    }

    private static <T> Function2<T, Function1<T, T>, T> andCall() {
        return new Function2<T, Function1<T, T>, T>() {
            @Override
            public T call(T data, Function1<T, T> callable) throws Exception {
                return callable.call(data);
            }
        };
    }

    private static <Fields extends ImmutableMap<String, String>> Function1<ImmutableMap<Definition, ImmutableList<Fields>>, ImmutableMap<Definition, ImmutableList<Fields>>> put(final Definition definition, final ImmutableList<Fields> newRecords) {
        return new Function1<ImmutableMap<Definition, ImmutableList<Fields>>, ImmutableMap<Definition, ImmutableList<Fields>>>() {
            @Override
            public ImmutableMap<Definition, ImmutableList<Fields>> call(ImmutableMap<Definition, ImmutableList<Fields>> data) throws Exception {
                return data.put(definition, newRecords.joinTo(listFor(data, definition)));
            }
        };
    }

    private Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> removeAll(final Definition definition, final Predicate<? super Record> predicate) {
        return new Function1<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>>() {
            @Override
            public ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> call(ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> data) throws Exception {
                return data.put(definition, listFor(data, definition).removeAll(matches(data, definition, predicate)));
            }
        };
    }

    private ImmutableList<ImmutableMap<String, String>> matches(ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> data, Definition definition, Predicate<? super Record> predicate) {
        return matches(predicate, sequence(listFor(data, definition)).map(STMRecords.this.asRecord(definition)));
    }
}
