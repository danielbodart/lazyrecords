package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Seconds;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;

public class SimplePool implements SearcherPool {
    private final Directory directory;
    private final Clock clock;
    private Searcher clean;
    private Searcher dirty;
    private Date created;
    private StaleSearcherSeconds seconds;

    public SimplePool(Directory directory, Clock clock, StaleSearcherSeconds seconds) throws IOException {
        this.directory = directory;
        this.clock = clock;
        this.seconds = seconds;
        clean = create();
    }

    @Override
    public synchronized Searcher searcher() throws IOException {
        if (readerIsStale()) {
            return createAndSwap();
        }
        return clean;
    }

    private Searcher createAndSwap() throws IOException {
        close(dirty);
        dirty = clean;
        return clean = create();
    }

    private Searcher create() throws IOException {
        created = clock.now();
        return new IgnoreCloseSearcher(new LuceneSearcher(new IndexSearcher(directory)));
    }

    private boolean readerIsStale() {
        return Seconds.between(created, clock.now()) >= seconds.value();
    }

    @Override
    public synchronized void markAsDirty() {

    }

    @Override
    public synchronized void close() throws IOException {
        close(dirty);
        close(clean);
    }

    private void close(Closeable closeable) {
        try {
            Closeables.close(closeable);
        } catch (Exception ignored) {
        }
    }

    public static class StaleSearcherSeconds implements Value<Integer> {
        public static final String PROPERTY = "lazyrecords.simplepool.stale.searcher.seconds";
        private final Integer value;

        public StaleSearcherSeconds(Integer value) {
            this.value = value;
        }

        public StaleSearcherSeconds() {
            this(Integer.parseInt(System.getProperty(PROPERTY, "60")));
        }

        @Override
        public Integer value() {
            return value;
        }
    }

    private static class IgnoreCloseSearcher implements Searcher {
        private final Searcher searcher;

        public IgnoreCloseSearcher(Searcher searcher) {
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
            // ignore
        }
    }
}
