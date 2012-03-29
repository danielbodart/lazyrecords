package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.io.Closeable;
import java.io.IOException;

public class SchemaGeneratingRecords implements Records, Closeable{
    private final Records records;
    private final Schema schema;

    public SchemaGeneratingRecords(final Records records, final Schema schema) {
        this.records = records;
        this.schema = schema;
    }

    @Override
    public void close() throws IOException {
        Closeables.close(records);
    }

    @Override
    public Number add(Definition definition, Record... records) {
        return records(definition).add(definition, records);
    }

    @Override
    public Number add(Definition definition, Sequence<Record> records) {
        return records(definition).add(definition, records);
    }

    @Override
    public Number set(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return records(definition).set(definition, records);
    }

    @Override
    public Number set(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return records(definition).set(definition, records);
    }

    @Override
    public Number put(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return records(definition).put(definition, records);
    }

    @Override
    public Number put(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return records(definition).put(definition, records);
    }

    @Override
    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        return records(definition).remove(definition, predicate);
    }

    @Override
    public Number remove(Definition definition) {
        return records(definition).remove(definition);
    }

    @Override
    public Sequence<Record> get(Definition definition) {
        return records(definition).get(definition);
    }

    private Records records(Definition definition) {
        if(!schema.exists(definition)){
            schema.define(definition);
        }
        return records;
    }
}
