package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Iterators;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import static com.googlecode.lazyrecords.lucene.Lucene.and;

public class LuceneSequence extends Sequence<Record> {
    private final LuceneStorage storage;
    private final Query query;
    private final Logger logger;
    private final Lucene lucene;
    private final Callable1<? super Document, Record> documentToRecord;
    private final CloseableList closeables;
    private final Sort sort;
    private final Value<Iterator<Record>> iterator;

    public LuceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                          final Callable1<? super Document, Record> documentToRecord, final Logger logger, CloseableList closeables) {
        this(lucene, storage, query, documentToRecord, logger, closeables, new Sort());
    }

    public LuceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                          final Callable1<? super Document, Record> documentToRecord, final Logger logger, final CloseableList closeables, final Sort sort) {
        this.lucene = lucene;
        this.storage = storage;
        this.query = query;
        this.documentToRecord = documentToRecord;
        this.logger = logger;
        this.closeables = closeables;
        this.sort = sort;
        this.iterator = new Function<Iterator<Record>>() {
            @Override
            public Iterator<Record> call() throws Exception {
                return Iterators.memorise(new LuceneIterator(storage, query, sort, documentToRecord, closeables, logger));
            }
        }.lazy();
    }

    public Iterator<Record> iterator() {
        return iterator.value();
    }

    @Override
    public Sequence<Record> filter(Predicate<? super Record> predicate) {
        return new LuceneSequence(lucene, storage, and(query, lucene.query(predicate)), documentToRecord, logger, closeables, sort);
    }

    @Override
    public int size() {
        try {
            return storage.count(query);
        } catch (IOException e) {
            throw LazyException.lazyException(e);
        }
    }

    @Override
    public Sequence<Record> sortBy(Comparator<? super Record> comparator) {
        return new LuceneSequence(lucene, storage, query, documentToRecord, logger, closeables, Lucene.sort(comparator));
    }
}
