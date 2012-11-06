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
        if (sortSpecified(sort)) {
            return searcher.search(query, Integer.MAX_VALUE, sort);
        } else {
            return searchWithoutSort(query);
        }
    }

    private boolean sortSpecified(Sort sort) {
        return sort != Lucene.NO_SORT;
    }

    private TopDocs searchWithoutSort(Query query) throws IOException {
        NonScoringCollector results = new NonScoringCollector();
        searcher.search(query, results);
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
}
