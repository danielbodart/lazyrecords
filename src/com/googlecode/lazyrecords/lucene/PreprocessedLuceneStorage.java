package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.lucene.mappings.DelegatingStorage;
import org.apache.lucene.search.Query;

import java.io.IOException;

public class PreprocessedLuceneStorage extends DelegatingStorage {

    private final LuceneQueryVisitor queryVisitor;

    public PreprocessedLuceneStorage(LuceneStorage decorated, LuceneQueryPreprocessor queryPreprocessor) {
        super(decorated);
        this.queryVisitor = new LuceneQueryVisitor(queryPreprocessor);
    }

    @Override
    public Number delete(Query query) throws IOException {
        return storage.delete(queryVisitor.visit(query));
    }

    @Override
    public void deleteNoCount(Query query) throws IOException {
        storage.deleteNoCount(queryVisitor.visit(query));
    }

    @Override
    public int count(Query query) throws IOException {
        return storage.count(queryVisitor.visit(query));
    }

}
