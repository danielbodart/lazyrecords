package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.IgnoreLogger;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Queryable;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.search.Query;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.lazyrecords.Record.functions.merge;
import static com.googlecode.lazyrecords.lucene.Lucene.and;
import static com.googlecode.lazyrecords.lucene.Lucene.record;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class LuceneRecords extends AbstractRecords implements Queryable<Query>, Closeable {
    private final LuceneStorage storage;
    private final LuceneMappings mappings;
    private final Logger logger;
    private final Lucene lucene;
    private final CloseableList closeables;

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
        return LuceneSequence.luceneSequence(lucene, storage, query, mappings.asRecord(definitions), logger, closeables);
    }

    public Sequence<Record> get(final Definition definition) {
        return query(record(definition), definition.fields());
    }

    public Number add(final Definition definition, final Sequence<Record> records) {
        return process(new Function<Number>() {
            @Override
            public Number call() throws Exception {
                return internalAdd(definition, records);
            }
        });
    }

    private Number internalAdd(Definition definition, Sequence<Record> records) throws IOException {
        return storage.add(records.map(mappings.asDocument(definition)));
    }

    public Number remove(final Definition definition, final Predicate<? super Record> predicate) {
        return process(new Function<Number>() {
            @Override
            public Number call() throws Exception {
                return internalRemove(definition, predicate);
            }
        });
    }

    private Number internalRemove(Definition definition, Predicate<? super Record> predicate) throws IOException {
        return storage.delete(query(definition, predicate));
    }

    public Number remove(final Definition definition) {
        return process(new Function<Number>() {
            @Override
            public Number call() throws Exception {
                return storage.delete(record(definition));
            }
        });
    }

    @Override
    public Number put(final Definition definition, final Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return process(new Function<Number>() {
            @Override
            public Number call() throws Exception {
                return records.map(update(definition)).reduce(sum());
            }
        });
    }

    private Function1<Pair<? extends Predicate<? super Record>, Record>, Number> update(final Definition definition) {
        return new Function1<Pair<? extends Predicate<? super Record>, Record>, Number>() {
            public Number call(Pair<? extends Predicate<? super Record>, Record> pair) throws Exception {
                Predicate<? super Record> predicate = pair.first();
                Sequence<Record> matched = get(definition).filter(predicate).realise();
                Record updatedFields = Record.methods.filter(pair.second(), definition.fields());
                if (matched.isEmpty()) {
                    return internalAdd(definition, one(updatedFields));
                }
                storage.deleteNoCount(query(definition, predicate));
                return internalAdd(definition, matched.map(merge(updatedFields)));
            }
        };
    }

    private Query query(Definition definition, Predicate<? super Record> predicate) {
        return and(record(definition), lucene.query(predicate));
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

    private Number process(Callable<Number> callable) {
        try {
            try {
                return callable.call();
            } finally {
                storage.flush();
            }
        } catch (Exception e) {
            throw LazyException.lazyException(e);
        }
    }
}
