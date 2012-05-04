package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.googlecode.lazyrecords.Definition.functions.sortFields;
import static com.googlecode.totallylazy.Sequences.sequence;

public class MemoryRecords extends AbstractRecords {
    private final ConcurrentMap<Definition, List<Record>> memory = new ConcurrentHashMap<Definition, List<Record>>();

    public Sequence<Record> get(Definition definition) {
        return sequence(recordsFor(definition));
    }

    private List<Record> recordsFor(Definition definition) {
        memory.putIfAbsent(definition, new CopyOnWriteArrayList<Record>());
        return memory.get(definition);
    }

    public Number add(final Definition definition, Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }

        List<Record> newRecords = records.map(sortFields(definition)).toList();
        List<Record> list = recordsFor(definition);
        list.addAll(newRecords);
        
        return newRecords.size();
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        List<Record> matches = sequence(recordsFor(definition)).
                filter(predicate).
                toList();

        recordsFor(definition).removeAll(matches);

        return matches.size();
    }
}
