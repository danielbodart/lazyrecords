package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.first;
import static com.googlecode.totallylazy.Sequences.sequence;

public class RecordDefinition implements Definition {
    private final String name;
    private final Sequence<Keyword<?>> fields;

    // See Definition.constructors.definition to construct
    RecordDefinition(final String name, final Iterable<? extends Keyword<?>> fields) {
        this.name = name;
        this.fields = sequence(fields);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return fields;
    }
}
