package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.iterators.StatefulIterator;
import com.googlecode.lazyrecords.Record;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.AlreadyClosedException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import static com.googlecode.totallylazy.Arrays.containsIndex;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;

public class LuceneIterator extends StatefulIterator<Record> implements Closeable{
    private final LuceneStorage storage;
    private final Query query;
    private final Sort sort;
    private final Callable1<? super Document, Record> documentToRecord;
    private final Logger logger;
    private ScoreDoc[] scoreDocs;
    private int index = 0;
    private Searcher searcher;
    private boolean closed = false;

    public LuceneIterator(LuceneStorage storage, Query query, Sort sort, Callable1<? super Document, Record> documentToRecord, Logger logger) {
        this.storage = storage;
        this.query = query;
        this.sort = sort;
        this.documentToRecord = documentToRecord;
        this.logger = logger;
    }

    @Override
    protected Record getNext() throws Exception {
        if(!containsIndex(scoreDocs(), index)){
            close();
            return finished();
        }
        Document document = searcher().document(scoreDocs()[index++].doc);
        return documentToRecord.call(document);
    }

    private ScoreDoc[] scoreDocs() throws IOException {
        if( scoreDocs == null) {
            Map<String,Object> log = Maps.<String, Object>map(pair(Loggers.TYPE, Loggers.LUCENE), pair(Loggers.EXPRESSION, query));
            long start = System.nanoTime();
            scoreDocs = searcher().search(query, sort).scoreDocs;
            log.put(Loggers.MILLISECONDS, calculateMilliseconds(start, System.nanoTime()));
            log.put(Loggers.COUNT, scoreDocs.length);
            logger.log(log);
        }
        return scoreDocs;
    }

    private Searcher searcher() throws IOException {
        if(closed){
            throw new AlreadyClosedException("This iterator has already been closed");
        }

        if( searcher == null){
            searcher = storage.searcher();
        }
        return searcher;
    }

    @Override
    public void close() throws IOException {
        Closeables.close(searcher);
        searcher = null;
        closed = true;
    }
}
