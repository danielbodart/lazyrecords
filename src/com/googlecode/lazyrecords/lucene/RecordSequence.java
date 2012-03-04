package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Record;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import java.util.Iterator;

import static com.googlecode.lazyrecords.lucene.Lucene.and;

public class RecordSequence extends Sequence<Record> {
    private final LuceneStorage storage;
    private final Query query;
    private final Logger logger;
    private final Lucene lucene;
    private final Callable1<? super Document,Record> documentToRecord;
    private CloseableList closeables;

    public RecordSequence(final Lucene lucene, final LuceneStorage storage, final Query query,
                          final Callable1<? super Document, Record> documentToRecord, final Logger logger, CloseableList closeables) {
        this.lucene = lucene;
        this.storage = storage;
        this.query = query;
        this.documentToRecord = documentToRecord;
        this.logger = logger;
        this.closeables = closeables;
    }


    public Iterator<Record> iterator() {
        return closeables.manage(new LuceneIterator(storage, query, documentToRecord, logger));
    }

    @Override
    public Sequence<Record> filter(Predicate<? super Record> predicate) {
        return new RecordSequence(lucene, storage, and(query, lucene.query(predicate)), documentToRecord, logger, closeables);
    }
}
