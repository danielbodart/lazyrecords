package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
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

    public LuceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                          final Callable1<? super Document, Record> documentToRecord, final Logger logger, CloseableList closeables) {
        this(lucene, storage, query, documentToRecord, logger, closeables, new Sort());
    }

    public LuceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                          final Callable1<? super Document, Record> documentToRecord, final Logger logger, CloseableList closeables, Sort sort) {
        this.lucene = lucene;
        this.storage = storage;
        this.query = query;
        this.documentToRecord = documentToRecord;
        this.logger = logger;
        this.closeables = closeables;
        this.sort = sort;
    }

    public Iterator<Record> iterator() {
        return closeables.manage(new LuceneIterator(storage, query, sort, documentToRecord, logger));
    }

    @Override
    public Sequence<Record> filter(Predicate<? super Record> predicate) {
        return new LuceneSequence(lucene, storage, and(query, lucene.query(predicate)), documentToRecord, logger, closeables, sort);
    }

    @Override
    public Number size() {
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
