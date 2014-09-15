package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Callable1;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

public class PooledSearcher implements Searcher {
    private final Searcher searcher;
    private final Callable1<Searcher, ?> checkin;

    public PooledSearcher(Searcher searcher, Callable1<Searcher, ?> checkin) {
        this.searcher = searcher;
        this.checkin = checkin;
    }

    @Override
    public TopDocs search(Query query, Sort sort) throws IOException {
        return searcher.search(query, sort);
    }

    @Override
    public TopDocs search(Query query, Sort sort, int end) throws IOException {
        return searcher.search(query, sort, end);
    }

    @Override
    public void search(Query query, Collector collector) throws IOException {
        searcher.search(query, collector);
    }

    @Override
    public Document document(int id) throws IOException {
        return searcher.document(id);
    }

    @Override
    public int count(Query query) throws IOException {
        return searcher.count(query);
    }

    @Override
    public void close() throws IOException {
        try {
            checkin.call(this);
        } catch (Exception ignored) {
        }
    }
}
