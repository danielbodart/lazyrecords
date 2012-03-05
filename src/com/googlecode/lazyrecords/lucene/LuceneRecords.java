package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.IgnoreLogger;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Queryable;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import org.apache.lucene.search.Query;

import java.io.Closeable;
import java.io.IOException;

import static com.googlecode.lazyrecords.lucene.Lucene.and;
import static com.googlecode.lazyrecords.lucene.Lucene.record;

public class LuceneRecords extends AbstractRecords implements Queryable<Query>, Closeable {
    private final LuceneStorage storage;
    private final LuceneMappings mappings;
    private final Logger logger;
    private final Lucene lucene;
    private CloseableList closeables;

    public LuceneRecords(final LuceneStorage storage, final LuceneMappings mappings, final Logger logger) throws IOException {
        this.storage = storage;
        this.mappings = mappings;
        this.logger = logger;
        lucene = new Lucene(mappings.stringMappings());
        closeables = new CloseableList();
    }

    public LuceneRecords(final LuceneStorage storage) throws IOException {
        this(storage, new LuceneMappings(), new IgnoreLogger());
    }

    public Sequence<Record> query(final Query query, final Sequence<Keyword<?>> definitions) {
        return new LuceneSequence(lucene, storage, query, mappings.asRecord(definitions), logger, closeables);
    }

    public Sequence<Record> get(final Definition definition) {
        return query(record(definition), definition.fields());
    }

    public Number add(Definition definition, Sequence<Record> records) {
        try {
            return storage.add(records.map(mappings.asDocument(definition)));
        } catch (IOException e) {
            throw LazyException.lazyException(e);
        }
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        return remove(and(record(definition), lucene.query(predicate)));
    }

    public Number remove(Definition definition) {
        return remove(record(definition));
    }

    public Number remove(Query query) {
        try {
            return storage.delete(query);
        } catch (IOException e) {
            throw LazyException.lazyException(e);
        }
    }

    public int count(final Query query) {
        try {
            return storage.count(query);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public void close() throws IOException {
        closeables.close();
    }
}
