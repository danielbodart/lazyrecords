package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.googlecode.lazyrecords.Definition.functions.sortFields;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Sequences.sequence;

public class MemoryRecords extends AbstractRecords {
    private final ConcurrentMap<String, List<Map<String, String>>> data = new ConcurrentHashMap<String, List<Map<String, String>>>();
    private final StringMappings mappings;

    public MemoryRecords(StringMappings mappings) {
        this.mappings = mappings;
    }

    public MemoryRecords() {
        this(new StringMappings());
    }

    public Sequence<Record> get(Definition definition) {
        return sequence(recordsFor(definition)).map(asRecord(definition));
    }

    private List<Map<String, String>> recordsFor(Definition definition) {
        data.putIfAbsent(definition.name(), new CopyOnWriteArrayList<Map<String, String>>());
        return data.get(definition.name());
    }

    public Number add(final Definition definition, Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }

        List<Map<String, String>> newRecords = records.map(sortFields(definition)).map(asMap(definition)).toList();
        List<Map<String, String>> list = recordsFor(definition);
        list.addAll(newRecords);

        return newRecords.size();
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        List<Map<String, String>> matches = get(definition).
                filter(predicate).
                <Value<Map<String, String>>>unsafeCast().
                map(Callables.<Map<String, String>>value()).
                toList();

        recordsFor(definition).removeAll(matches);

        return matches.size();
    }

    private Callable1<Map<String, String>, Record> asRecord(final Definition definition) {
        return new Callable1<Map<String, String>, Record>() {
            @Override
            public Record call(Map<String, String> data) throws Exception {
                return SourceRecord.record(data, definition.fields().map(values(data)));
            }
        };
    }

    private Callable1<Keyword<?>, Pair<Keyword<?>, Object>> values(final Map<String, String> map) {
        return new Callable1<Keyword<?>, Pair<Keyword<?>, Object>>() {
            @Override
            public Pair<Keyword<?>, Object> call(Keyword<?> keyword) throws Exception {
                return Pair.<Keyword<?>, Object>pair(keyword, mappings.toValue(keyword.forClass(), map.get(keyword.name())));
            }
        };
    }

    private Callable1<Record, Map<String, String>> asMap(final Definition definition) {
        return new Callable1<Record, Map<String, String>>() {
            @Override
            public Map<String, String> call(Record record) throws Exception {
                return map(definition.fields().map(values(record)));
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
}
