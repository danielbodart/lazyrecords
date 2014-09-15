package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;

import java.io.IOException;

import static com.googlecode.totallylazy.Closeables.safeClose;

public class LuceneSearcher implements Searcher {
    private final IndexSearcher searcher;

    public LuceneSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public TopDocs search(Query query, Sort sort) throws IOException {
        return search(query, sort, Integer.MAX_VALUE);
    }

    @Override
    public TopDocs search(Query query, Sort sort, int end) throws IOException {
        if (sortSpecified(sort)) return searcher.search(query, end, sort);
        return this.search(query, end);
    }

    @Override
    public void search(Query query, Collector collector) throws IOException {
        searcher.search(query, collector);
    }

    public TopDocs search(Query query, int end) throws IOException {
        NonScoringCollector results = new NonScoringCollector(end);
        search(query, results);
        return results.topDocs();
    }

    @Override
    public Document document(int id) throws IOException {
        return searcher.doc(id);
    }

    @Override
    public int count(Query query) throws IOException {
        TotalHitCountCollector results = new TotalHitCountCollector();
        searcher.search(query, results);
        return results.getTotalHits();
    }

    @Override
    public void close() throws IOException {
        safeClose(searcher);
    }

    public IndexSearcher searcher() {
        return searcher;
    }

    private boolean sortSpecified(Sort sort) {
        return sort != Lucene.NO_SORT;
    }
}