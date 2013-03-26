package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Closeables;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.concurrent.Callable;

import static com.googlecode.totallylazy.Closeables.safeClose;

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

    @Override
    public TopDocs search(Query query, Sort sort, int end) throws IOException {
        try {
            return searcher.search(query, sort, end);
        } catch (Exception e) {
            replaceBrokenSearcher();
            return search(query, sort, end);
        }
    }

    private synchronized void replaceBrokenSearcher() {
        safeClose(searcher);
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
    public int count(Query query) throws IOException {
        try {
            return searcher.count(query);
        } catch (Exception e) {
            replaceBrokenSearcher();
            return count(query);
        }
    }

    @Override
    public void close() throws IOException {
        Closeables.close(searcher);
    }
}
