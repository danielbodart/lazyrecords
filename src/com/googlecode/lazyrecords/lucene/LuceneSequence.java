package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
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
    private final Lazy<Iterable<Record>> data;
    private final int start;
    private final int end;

    private LuceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                           final Callable1<? super Document, Record> documentToRecord, final Logger logger,
                           final CloseableList closeables, final Sort sort, final int start, final int end) {
        this.lucene = lucene;
        this.storage = storage;
        this.query = query;
        this.documentToRecord = documentToRecord;
        this.logger = logger;
        this.closeables = closeables;
        this.sort = sort;
        this.start = start;
        this.end = end;
        this.data = new Lazy<Iterable<Record>>() {
            @Override
            protected Iterable<Record> get() throws Exception {
                return Computation.memorise(new LuceneIterator(storage, query, sort, documentToRecord, start, end, closeables, logger));
            }
        };
    }

    public static Sequence<Record> luceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query, final Callable1<? super Document, Record> documentToRecord, final Logger logger, final CloseableList closeables, final Sort sort, final int start, final int end) {
        if(end <= start) return Sequences.empty();
        return new LuceneSequence(lucene, storage, query, documentToRecord, logger, closeables, sort, start, end);
    }

    public static Sequence<Record> luceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query, final Callable1<? super Document, Record> documentToRecord, final Logger logger, CloseableList closeables) {
        return new LuceneSequence(lucene, storage, query, documentToRecord, logger, closeables, Lucene.NO_SORT, 0, Integer.MAX_VALUE);
    }

    public Iterator<Record> iterator() {
        return data.value().iterator();
    }

    @Override
    public Sequence<Record> filter(Predicate<? super Record> predicate) {
        return luceneSequence(lucene, storage, and(query, lucene.query(predicate)), documentToRecord, logger, closeables, sort, start, end);
    }

    @Override
    public int size() {
        try {
            return Math.max(0, Math.min(end, storage.count(query)) - start);
        } catch (IOException e) {
            throw LazyException.lazyException(e);
        }
    }

    @Override
    public Sequence<Record> sortBy(Comparator<? super Record> comparator) {
        return luceneSequence(lucene, storage, query, documentToRecord, logger, closeables, Sorting.sort(comparator), start, end);
    }

    @Override
    public Sequence<Record> drop(int count) {
        return luceneSequence(lucene, storage, query, documentToRecord, logger, closeables, sort, start + count, end);
    }

    @Override
    public Sequence<Record> take(int count) {
        return luceneSequence(lucene, storage, query, documentToRecord, logger, closeables, sort, start, start + count);
    }
}
