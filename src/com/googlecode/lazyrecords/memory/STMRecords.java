package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.ToRecord;
import com.googlecode.lazyrecords.Transaction;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.collections.PersistentList;
import com.googlecode.totallylazy.collections.PersistentMap;
import com.googlecode.totallylazy.collections.PersistentSortedMap;

import java.util.ConcurrentModificationException;

import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.lazyrecords.Record.methods.filter;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.reverse;
import static com.googlecode.totallylazy.Sequences.sequence;

public class STMRecords extends AbstractRecords implements Transaction {
    private final StringMappings mappings;
    private final STM stm;
    private STM snapshot;
    private PersistentList<Pair<Integer, Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Pair<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Integer>>>> modifications;

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
        return modifyReturn(put(definition, newRecords));
    }

    public Number remove(final Definition definition, Predicate<? super Record> predicate) {
        return modifyReturn(removeAll(definition, predicate));
    }


    private static PersistentList<PersistentMap<String, String>> matches(Predicate<? super Record> predicate, Sequence<Record> records) {
        return records.filter(predicate).
                <Value<PersistentMap<String, String>>>unsafeCast().
                map(Callables.<PersistentMap<String, String>>value()).
                toPersistentList();
    }

    @Override
    public Number set(final Definition definition, final Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return modifyReturn(new Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Pair<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Integer>>() {
            @Override
            public Pair<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Integer> call(PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> database) throws Exception {
                PersistentList<PersistentMap<String, String>> table = listFor(database, definition);

                final int[] count = {0};

                PersistentList<PersistentMap<String, String>> result = table.map(new Mapper<PersistentMap<String, String>, PersistentMap<String, String>>() {
                    @Override
                    public PersistentMap<String, String> call(PersistentMap<String, String> row) throws Exception {
                        for (Pair<? extends Predicate<? super Record>, Record> pair : records) {
                            Record original = asRecord(definition, row);
                            if (pair.first().matches(original)) {
                                row = asPersistentMap(definition, merge(filter(pair.second(), definition.fields())).call(original));
                                count[0]++;
                            }
                        }
                        return row;
                    }
                });

                return Pair.pair(database.insert(definition, result), count[0]);
            }
        });
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

    private Integer modifyReturn(Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Pair<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Integer>> callable) {
        Integer modified = snapshot.modifyReturn(callable);
        modifications = modifications.cons(Pair.pair(modified, callable));
        return modified;
    }

    private static <M extends PersistentMap<String, String>> PersistentList<M> listFor(PersistentMap<Definition, PersistentList<M>> data, Definition definition) {
        return data.lookup(definition).getOrElse(PersistentList.constructors.<M>empty());
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
                return Pair.<Keyword<?>, Object>pair(keyword, mappings.toValue(keyword.forClass(), map.lookup(keyword.name()).getOrNull()));
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
                return pair(keyword.name(), mappings.toString(keyword.forClass(), record.get(keyword)));
            }
        };
    }

    private static <T, R> Function1<T, T> applyAll(final Iterable<? extends Pair<R, ? extends Function1<T, Pair<T, R>>>> callables) {
        return new Function1<T, T>() {
            @Override
            public T call(T data) throws Exception {
                return sequence(callables).fold(data, new Function2<T, Pair<R, ? extends Function1<T, Pair<T, R>>>, T>() {
                    @Override
                    public T call(T t, Pair<R, ? extends Function1<T, Pair<T, R>>> modification) throws Exception {
                        R expected = modification.first();
                        Pair<T, R> result = modification.second().call(t);
                        R actual = result.second();
                        if (!expected.equals(actual))
                            throw new ConcurrentModificationException("Expected:" + expected + " Actual:" + actual);
                        return result.first();

                    }
                });
            }
        };
    }

    private static <Fields extends PersistentMap<String, String>> Function1<PersistentMap<Definition, PersistentList<Fields>>, Pair<PersistentMap<Definition, PersistentList<Fields>>, Integer>> put(final Definition definition, final PersistentList<Fields> newRecords) {
        return new Function1<PersistentMap<Definition, PersistentList<Fields>>, Pair<PersistentMap<Definition, PersistentList<Fields>>, Integer>>() {
            @Override
            public Pair<PersistentMap<Definition, PersistentList<Fields>>, Integer> call(PersistentMap<Definition, PersistentList<Fields>> data) throws Exception {
                return Pair.pair(data.insert(definition, newRecords.joinTo(listFor(data, definition))), newRecords.size());
            }
        };
    }

    private Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Pair<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Integer>> removeAll(final Definition definition, final Predicate<? super Record> predicate) {
        return new Function1<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Pair<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Integer>>() {
            @Override
            public Pair<PersistentMap<Definition, PersistentList<PersistentMap<String, String>>>, Integer> call(PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> data) throws Exception {
                PersistentList<PersistentMap<String, String>> rows = matches(data, definition, predicate);
                return pair(data.insert(definition, listFor(data, definition).deleteAll(rows)), rows.size());
            }
        };
    }

    private PersistentList<PersistentMap<String, String>> matches(PersistentMap<Definition, PersistentList<PersistentMap<String, String>>> data, Definition definition, Predicate<? super Record> predicate) {
        return matches(predicate, sequence(listFor(data, definition)).map(STMRecords.this.asRecord(definition)));
    }
}
