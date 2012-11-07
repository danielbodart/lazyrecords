package com.googlecode.lazyrecords.lucene.mappings;

import com.googlecode.lazyrecords.lucene.LuceneStorage;
import com.googlecode.lazyrecords.lucene.Searcher;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public abstract class DelegatingStorage implements LuceneStorage {
    protected final LuceneStorage storage;

    protected DelegatingStorage(LuceneStorage storage) {
        this.storage = storage;
    }

    @Override
    public Number add(final Sequence<Document> documents) throws IOException { return storage.add(documents); }

    @Override
    public Number delete(final Query query) throws IOException { return storage.delete(query); }

    @Override
    public void deleteAll() throws IOException { storage.deleteAll(); }

    @Override
    public void deleteNoCount(Query query) throws IOException {storage.deleteNoCount(query);}

    @Override
    public int count(Query query) throws IOException {return storage.count(query);}

    @Override
    public <T> T search(Callable1<Searcher, T> callable) throws IOException {return storage.search(callable);}

    @Override
    public Searcher searcher() throws IOException {return storage.searcher();}

    @Override
    public void backup(File destination) throws Exception {storage.backup(destination);}

    @Override
    public void restore(File file) throws Exception {storage.restore(file);}

    @Override
    public void close() throws IOException {storage.close();}

    @Override
    public void flush() throws IOException {storage.flush();}
}
