package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

public class ManagedSearcher implements Searcher {
    private final SearcherManager manager;
    public final LuceneSearcher searcher;

    public ManagedSearcher(SearcherManager manager) {
        this.manager = manager;
        this.searcher = new LuceneSearcher(manager.acquire());
    }

    @Override
    public TopDocs search(Query query, Sort sort) throws IOException {
        return searcher.search(query, sort);
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
        manager.release(searcher.searcher());
    }
}
