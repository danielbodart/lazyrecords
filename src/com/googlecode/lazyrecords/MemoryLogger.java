package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.collections.PersistentList;

import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;

public class MemoryLogger implements Logger {
    private volatile PersistentList<Map<String, ?>> data;

    public MemoryLogger() {
        forget();
    }

    public MemoryLogger forget() {
        data = PersistentList.constructors.empty();
        return this;
    }

    @Override
    public Logger log(Map<String, ?> parameters) {
        data = data.cons(parameters);
        return this;
    }

    public Sequence<Map<String, ?>> data() {
        return sequence(data);
    }
}
