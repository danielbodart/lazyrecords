package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Value;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.SearcherManager;

import java.io.IOException;

import static com.googlecode.totallylazy.Closeables.safeClose;

public class LucenePool implements SearcherPool {
    private final Value<SearcherManager> manager;

    public LucenePool(final IndexWriter writer) {
        this.manager = createSearchManagerLazily(writer);
    }

    @Override
    public Searcher searcher() throws IOException {
        return new ManagedSearcher(manager());
    }

    @Override
    public void markAsDirty() throws IOException {
        manager().maybeRefresh();
    }

    @Override
    public void close() throws IOException {
        safeClose(manager());
    }

    private SearcherManager manager() {
        return manager.value();
    }

    private Value<SearcherManager> createSearchManagerLazily(final IndexWriter writer) {
        return new Function<SearcherManager>() {
            @Override
            public SearcherManager call() throws Exception {
                return new SearcherManager(writer, true, null);
            }
        }.lazy();
    }
}
