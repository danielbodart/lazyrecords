package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.io.Closeable;
import java.io.IOException;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.LazyException.lazyException;

public class LucenePartitionedRecords implements Records, Closeable {
    private final PartitionedIndex partitionedIndex;
    private final LuceneMappings mappings;
    private final Logger logger;
    private final LuceneQueryPreprocessor preprocessor;
    private final CloseableList closeables = new CloseableList();

    public LucenePartitionedRecords(PartitionedIndex partitionedIndex, LuceneMappings mappings, Logger logger) {
        this(partitionedIndex, mappings, logger, new DoNothingLuceneQueryPreprocessor());
    }

    public LucenePartitionedRecords(PartitionedIndex partitionedIndex, LuceneMappings mappings, Logger logger, LuceneQueryPreprocessor preprocessor) {
        this.partitionedIndex = partitionedIndex;
        this.mappings = mappings;
        this.logger = logger;
        this.preprocessor = preprocessor;
    }

    private LuceneRecords recordsFor(Definition definition) {
        return closeables.manage(createRecords(definition));
    }

    private LuceneRecords createRecords(Definition definition) {
        try {
            return new LuceneRecords(partitionedIndex.partition(definition), mappings, logger, preprocessor);
        } catch (IOException e) {
            throw lazyException(e);
        }
    }

    private Number process(Definition definition, Callable1<Records, Number> callable) {
        return using(createRecords(definition), callable);
    }

    @Override
    public Number add(final Definition definition, final Record... records) {
        return process(definition, functions.add(definition, records));
    }

    @Override
    public Number add(Definition definition, Sequence<Record> records) {
        return process(definition, functions.add(definition, records));
    }

    @Override
    public Number set(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return process(definition, functions.set(definition, records));
    }

    @Override
    public Number set(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return process(definition, functions.set(definition, records));
    }

    @Override
    public Number put(Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return process(definition, functions.put(definition, records));
    }

    @Override
    public Number put(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return process(definition, functions.put(definition, records));
    }

    @Override
    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        return process(definition, functions.remove(definition, predicate));
    }

    @Override
    public Number remove(Definition definition) {
        return process(definition, functions.remove(definition));
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
