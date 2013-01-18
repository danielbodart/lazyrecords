package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.ToRecord;
import com.googlecode.lazyrecords.Transaction;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.totallylazy.collections.PersistentMap;
import com.googlecode.totallylazy.collections.PersistentSortedMap;

import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.lazyrecords.Record.methods.filter;
import static com.googlecode.totallylazy.Sequences.reverse;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class STMRecords extends AbstractRecords implements Transaction {
    private final StringMappings mappings;
    private final STM stm;
    private STM snapshot;
    private PersistentList<Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>,
            PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>>> modifications;

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

        PersistentList<PersistentMap<String, String>> newRecords = records.
                map(asPersistentMap(definition)).
                toPersistentList();
        modify(put(definition, newRecords));

        return newRecords.size();
    }

    public Number remove(final Definition definition, Predicate<? super Record> predicate) {
        PersistentList<PersistentMap<String, String>> matches = matches(predicate, get(definition));

        modify(removeAll(definition, predicate));

        return matches.size();
    }


    private static PersistentList<PersistentMap<String, String>> matches(Predicate<? super Record> predicate, Sequence<Record> records) {
        return records.filter(predicate).
                    <Value<PersistentMap<String, String>>>unsafeCast().
                    map(Callables.<PersistentMap<String, String>>value()).
                    toPersistentList();
    }

    @Override
    public Number set(final Definition definition, final Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        modify(new Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>>() {
            @Override
            public PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> call(PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> data) throws Exception {
                return data.put(definition, listFor(data, definition).map(new Function1<PersistentMap<String, String>, PersistentMap<String, String>>() {
                    @Override
                    public PersistentMap<String, String> call(PersistentMap<String, String> row) throws Exception {
                        return records.fold(row, new Function2<PersistentMap<String, String>, Pair<? extends Predicate<? super Record>, Record>, PersistentMap<String, String>>() {
                            @Override
                            public PersistentMap<String, String> call(PersistentMap<String, String> fields, Pair<? extends Predicate<? super Record>, Record> pair) throws Exception {
                                Record original = asRecord(definition, fields);
                                if (pair.first().matches(original)) return asPersistentMap(definition, merge(filter(pair.second(), definition.fields())).call(original));
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
        modifications = PersistentList.constructors.empty();
    }

    private void modify(Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>> callable) {
        snapshot.modify(callable);
        modifications = modifications.cons(callable);
    }

    private static <M extends PersistentMap<String, String>> PersistentList<M> listFor(PersistentMap<Definition, PersistentList<M>> data, Definition definition) {
        return data.get(definition).getOrElse(PersistentList.constructors.<M>empty());
    }

    private ToRecord<PersistentMap<String, String>> asRecord(final Definition definition) {
        return new ToRecord<PersistentMap<String, String>>() {
            @Override
            public Record call(PersistentMap<String, String> data) throws Exception {
                return asRecord(definition, data);
            }
        };
    }

    private Record asRecord(Definition definition, PersistentMap<String, String> data) {
        return SourceRecord.record(data, definition.fields().map(values(data)));
    }

    private Function1<Keyword<?>, Pair<Keyword<?>, Object>> values(final PersistentMap<String, String> map) {
        return new Function1<Keyword<?>, Pair<Keyword<?>, Object>>() {
            @Override
            public Pair<Keyword<?>, Object> call(Keyword<?> keyword) throws Exception {
                return Pair.<Keyword<?>, Object>pair(keyword, mappings.toValue(keyword.forClass(), map.get(keyword.name()).getOrNull()));
            }
        };
    }

    private RecordTo<PersistentMap<String, String>> asPersistentMap(final Definition definition) {
        return new RecordTo<PersistentMap<String, String>>() {
            @Override
            public PersistentMap<String, String> call(Record record) throws Exception {
                return STMRecords.this.asPersistentMap(definition, record);
            }
        };
    }

    private PersistentMap<String, String> asPersistentMap(Definition definition, Record record) {
        return PersistentSortedMap.constructors.sortedMap(definition.fields().map(values(record)));
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

    private static <Fields extends PersistentMap<String, String>> Function1<PersistentMap<Definition, PersistentList<Fields>>, PersistentMap<Definition, PersistentList<Fields>>> put(final Definition definition, final PersistentList<Fields> newRecords) {
        return new Function1<PersistentMap<Definition, PersistentList<Fields>>, PersistentMap<Definition, PersistentList<Fields>>>() {
            @Override
            public PersistentMap<Definition, PersistentList<Fields>> call(PersistentMap<Definition, PersistentList<Fields>> data) throws Exception {
                return data.put(definition, newRecords.joinTo(listFor(data, definition)));
            }
        };
    }

    private Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>> removeAll(final Definition definition, final Predicate<? super Record> predicate) {
        return new Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>>() {
            @Override
            public PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> call(PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> data) throws Exception {
                return data.put(definition, listFor(data, definition).removeAll(matches(data, definition, predicate)));
            }
        };
    }

    private PersistentList<PersistentMap<String, String>> matches(PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> data, Definition definition, Predicate<? super Record> predicate) {
        return matches(predicate, sequence(listFor(data, definition)).map(STMRecords.this.asRecord(definition)));
    }
}
