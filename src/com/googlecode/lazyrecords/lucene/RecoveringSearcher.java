package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Closeables;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.concurrent.Callable;

public class RecoveringSearcher implements Searcher {
    private Searcher searcher;
    private final Callable<Searcher> newSearcher;

    public RecoveringSearcher(Searcher searcher, Callable<Searcher> newSearcher) {
        this.searcher = searcher;
        this.newSearcher = newSearcher;
    }

    @Override
    public TopDocs search(Query query, Sort sort) throws IOException {
        try {
            return searcher.search(query, sort);
        } catch (Exception e) {
            replaceBrokenSearcher();
            return search(query, sort);
        }
    }

    private synchronized void replaceBrokenSearcher() {
        try {
            searcher.close();
        } catch (Exception e) {
            // don-t care as it's already broken
        }
        this.searcher = Callers.call(newSearcher);
    }

    @Override
    public Document document(int id) throws IOException {
        try {
            return searcher.document(id);
        } catch (Exception e) {
            replaceBrokenSearcher();
            return document(id);
        }
    }

    @Override
    public void close() throws IOException {
        Closeables.close(searcher);
    }
}
