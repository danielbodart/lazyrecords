package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Computation;
import com.googlecode.totallylazy.Function;
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
    private final Value<Iterable<Record>> data;
    private final int start;

    public LuceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                          final Callable1<? super Document, Record> documentToRecord, final Logger logger, CloseableList closeables) {
        this(lucene, storage, query, documentToRecord, logger, closeables, new Sort(), 0);
    }

    public LuceneSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                          final Callable1<? super Document, Record> documentToRecord, final Logger logger, final CloseableList closeables, final Sort sort, final int start) {
        this.lucene = lucene;
        this.storage = storage;
        this.query = query;
        this.documentToRecord = documentToRecord;
        this.logger = logger;
        this.closeables = closeables;
        this.sort = sort;
        this.start = start;
        this.data = new Function<Iterable<Record>>() {
            @Override
            public Iterable<Record> call() throws Exception {
                return Computation.memorise(new LuceneIterator(storage, query, sort, documentToRecord, start, closeables, logger));
            }
        }.lazy();
    }

    public Iterator<Record> iterator() {
        return data.value().iterator();
    }

    @Override
    public Sequence<Record> filter(Predicate<? super Record> predicate) {
        return new LuceneSequence(lucene, storage, and(query, lucene.query(predicate)), documentToRecord, logger, closeables, sort, start);
    }

    @Override
    public int size() {
        try {
            return storage.count(query) - start;
        } catch (IOException e) {
            throw LazyException.lazyException(e);
        }
    }

    @Override
    public Sequence<Record> sortBy(Comparator<? super Record> comparator) {
        return new LuceneSequence(lucene, storage, query, documentToRecord, logger, closeables, Lucene.sort(comparator), start);
    }

    @Override
    public Sequence<Record> drop(int count) {
        return new LuceneSequence(lucene, storage, query, documentToRecord, logger, closeables, sort, start + count);
    }
}
