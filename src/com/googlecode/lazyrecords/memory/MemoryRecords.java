package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.googlecode.lazyrecords.Definition.methods.sortFields;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.increment;

public class MemoryRecords extends AbstractRecords {
    private final Map<Definition, List<Record>> memory = map();

    public Sequence<Record> get(Definition definition) {
        return sequence(recordsFor(definition));
    }

    private List<Record> recordsFor(Definition definition) {
        if (!memory.containsKey(definition)) {
            memory.put(definition, new ArrayList<Record>());
        }
        return memory.get(definition);
    }

    public Number add(final Definition definition, Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }

        List<Record> list = recordsFor(definition);
        Number count = 0;
        for (Record record : records) {
            list.add(sortFields(definition, record));
            count = increment(count);
        }
        return count;
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        List<Record> matches = sequence(recordsFor(definition)).
                filter(predicate).
                toList();

        recordsFor(definition).removeAll(matches);

        return matches.size();
    }
}
