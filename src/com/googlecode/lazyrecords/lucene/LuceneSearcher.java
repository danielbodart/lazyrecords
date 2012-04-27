package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

import static com.googlecode.totallylazy.Closeables.safeClose;

public class LuceneSearcher implements Searcher {
    private final IndexSearcher searcher;

    public LuceneSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public TopDocs search(Query query, Sort sort) throws IOException {
        return searcher.search(query, Integer.MAX_VALUE, sort);
    }

    @Override
    public Document document(int id) throws IOException {
        return searcher.doc(id);
    }

    @Override
    public void close() throws IOException {
        safeClose(searcher);
    }

    public IndexSearcher searcher() {
        return searcher;
    }
}
