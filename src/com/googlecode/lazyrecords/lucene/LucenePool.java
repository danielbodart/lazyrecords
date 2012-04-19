package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Value;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;

import java.io.IOException;

public class LucenePool implements SearcherPool {
    private final Value<SearcherManager> manager;

    public LucenePool(final Directory directory) {
        this.manager = createSearchManagerLazily(directory);
    }

    @Override
    public Searcher searcher() throws IOException {
        return new MySearcher(manager(), new LuceneSearcher(manager().acquire()));
    }

    @Override
    public void markAsDirty() {
        try {
            manager().maybeRefresh();
        } catch (IOException e) {
            throw LazyException.lazyException(e);
        }
    }

    @Override
    public void close() throws IOException {
        manager().close();
    }

    private SearcherManager manager() {
        return manager.value();
    }

    private Value<SearcherManager> createSearchManagerLazily(final Directory directory) {
        return new Function<SearcherManager>() {
            @Override
            public SearcherManager call() throws Exception {
                return new SearcherManager(directory, null);
            }
        }.lazy();
    }

    private static class MySearcher implements Searcher {
        private final SearcherManager manager;
        public final LuceneSearcher searcher;

        private MySearcher(SearcherManager manager, LuceneSearcher searcher) {
            this.manager = manager;
            this.searcher = searcher;
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
        public void close() throws IOException {
            manager.release(searcher.searcher());
        }
    }
}
