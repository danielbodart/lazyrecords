package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
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
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;

public class LuceneIterator extends StatefulIterator<Record> implements Closeable{
    private final LuceneStorage storage;
    private final Query query;
    private final Sort sort;
    private final Callable1<? super Document, Record> documentToRecord;
    private final CloseableList closeables;
    private final Logger logger;
    private ScoreDoc[] scoreDocs;
    private int index;
    private final int end;
    private Searcher searcher;
    private boolean closed = false;

    public LuceneIterator(LuceneStorage storage, Query query, Sort sort, Callable1<? super Document, Record> documentToRecord, int start, int end, CloseableList closeables, Logger logger) {
        this.storage = storage;
        this.query = query;
        this.sort = sort;
        this.documentToRecord = documentToRecord;
        this.index = start;
        this.end = end;
        this.closeables = closeables;
        this.logger = logger;
    }

    @Override
    protected Record getNext() throws Exception {
        if(!containsIndex(scoreDocs(), index)){
            close();
            closeables.remove(this);
            return finished();
        }
        Document document = searcher().document(scoreDocs()[index++].doc);
        return documentToRecord.call(document);
    }

    private ScoreDoc[] scoreDocs() throws IOException {
        if( scoreDocs == null) {
            Map<String,Object> log = Maps.<String, Object>map(pair(Loggers.TYPE, Loggers.LUCENE), pair(Loggers.EXPRESSION, query));
            long start = System.nanoTime();
            scoreDocs = searcher().search(query, sort, end).scoreDocs;
            log.put(Loggers.MILLISECONDS, calculateMilliseconds(start, System.nanoTime()));
            log.put(Loggers.ROWS, scoreDocs.length);
            logger.log(log);
        }
        return scoreDocs;
    }

    private Searcher searcher() throws IOException {
        if(closed){
            throw new AlreadyClosedException("This iterator has already been closed");
        }

        if( searcher == null){
            closeables.manage(this);
            searcher = storage.searcher();
        }
        return searcher;
    }

    @Override
    public void close() throws IOException {
        safeClose(searcher);
        searcher = null;
        scoreDocs = null;
        closed = true;
    }
}
