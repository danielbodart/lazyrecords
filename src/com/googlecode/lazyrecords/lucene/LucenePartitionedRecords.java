package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.io.Closeable;
import java.io.IOException;

public class LucenePartitionedRecords implements Records, Closeable {
    private final PartitionedIndex partitionedIndex;
    private final LuceneMappings mappings;
    private final Logger logger;
    private final CloseableList closeables = new CloseableList();

    public LucenePartitionedRecords(PartitionedIndex partitionedIndex, LuceneMappings mappings, Logger logger) {
        this.partitionedIndex = partitionedIndex;
        this.mappings = mappings;
        this.logger = logger;
    }

    private LuceneRecords recordsFor(Definition definition) {
        try {
            return closeables.manage(new LuceneRecords(partitionedIndex.partition(definition), mappings, logger));
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public Number add(Definition definition, Record... records) {
        return recordsFor(definition).add(definition, records);
    }

    @Override
    public Number add(Definition definition, Sequence<Record> records) {
        return recordsFor(definition).add(definition, records);
    }

    @Override
    public Number set(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return recordsFor(definition).set(definition, records);
    }

    @Override
    public Number set(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return recordsFor(definition).set(definition, records);
    }

    @Override
    public Number put(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return recordsFor(definition).put(definition, records);
    }

    @Override
    public Number put(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return recordsFor(definition).put(definition, records);
    }

    @Override
    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        return recordsFor(definition).remove(definition, predicate);
    }

    @Override
    public Number remove(Definition definition) {
        return recordsFor(definition).remove(definition);
    }

    @Override
    public Sequence<Record> get(Definition definition) {
        return recordsFor(definition).get(definition);
    }

    @Override
    public void close() throws IOException {
        closeables.close();
    }
}
