package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;

public class RecordDefinition extends AbstractMetadata<Definition> implements Definition {
    private final String name;
    private final Sequence<Keyword<?>> fields;

    // See Definition.constructors.definition to construct
    RecordDefinition(final String name, Record metadata, final Iterable<? extends Keyword<?>> fields) {
        super(metadata);
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
    public int hashCode() {
        return name.hashCode() * fields.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Definition) &&
                (name.equals(((Definition) obj).name())) &&
                (fields().equals(((Definition) obj).fields()));
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return fields;
    }

    @Override
    public int compareTo(Definition definition) {
        return name().compareTo(definition.name());
    }

    @Override
    public Definition metadata(Record record) {
        return new RecordDefinition(name, record, fields);
    }
}