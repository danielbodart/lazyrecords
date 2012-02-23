package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;

public class RecordDefinition implements Definition {
    private final String name;
    private final Sequence<Keyword<?>> fields;

    private RecordDefinition(final String name, final Sequence<Keyword<?>> fields) {
        this.name = name;
        this.fields = fields;
    }

    public static Definition definition(final String name, final Iterable<? extends Keyword<?>> fields) {
        return new RecordDefinition(name, sequence(fields));
    }

    public static Definition definition(final String name, final Keyword<?>... otherFields) {
        return new RecordDefinition(name, sequence(otherFields));
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
