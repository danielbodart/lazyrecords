package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.iterators.StatefulIterator;
import com.googlecode.lazyrecords.Record;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.AlreadyClosedException;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

import static com.googlecode.totallylazy.Arrays.containsIndex;
import static com.googlecode.totallylazy.Pair.pair;

public class LuceneIterator extends StatefulIterator<Record> implements Closeable{
    private final LuceneStorage storage;
    private final Query query;
    private final Callable1<? super Document, Record> documentToRecord;
    private final Logger logger;
    private ScoreDoc[] scoreDocs;
    private int index = 0;
    private Searcher searcher;
    private boolean closed = false;

    public LuceneIterator(LuceneStorage storage, Query query, Callable1<? super Document, Record> documentToRecord, Logger logger) {
        this.storage = storage;
        this.query = query;
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
            logger.log(Maps.map(pair("lucene", query)));
            scoreDocs = searcher().search(query).scoreDocs;
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
