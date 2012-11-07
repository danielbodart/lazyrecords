package com.googlecode.lazyrecords.lucene.mappings;

import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.totallylazy.Runnables;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class BackgroundStorage extends DelegatingStorage {
    private final ExecutorService service;

    public BackgroundStorage(LuceneStorage storage) {
        super(storage);
        service = newSingleThreadExecutor();
    }

    @Override
    public Number add(final Sequence<Document> documents) throws IOException {
        try {
            return service.submit(new Callable<Number>() {
                @Override
                public Number call() throws Exception {
                    return storage.add(documents);
                }
            }).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Number delete(final Query query) throws IOException {
        try {
            return service.submit(new Callable<Number>() {
                @Override
                public Number call() throws Exception {
                    return storage.delete(query);
                }
            }).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void deleteNoCount(final Query query) throws IOException {
        try {
            service.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    storage.deleteNoCount(query);
                    return Runnables.VOID;
                }
            }).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            service.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    storage.flush();
                    return Runnables.VOID;
                }
            }).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        service.shutdown();
    }
}
