package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;

public class MemoryLogger implements Logger {
    private final List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();

    @Override
    public Logger log(Map<String, ?> parameters) {
        data.add(parameters);
        return this;
    }

    public Sequence<Map<String, ?>> getData() {
        return sequence(data);
    }
}
