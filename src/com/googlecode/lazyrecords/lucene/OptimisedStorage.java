package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Runnables.VOID;

public class OptimisedStorage implements LuceneStorage {
    private final Object lock = new Object();

    private final SearcherPool pool;
    private IndexWriter writer;

    public OptimisedStorage(IndexWriter indexWriter) {
        writer = indexWriter;
        pool = new LucenePool(indexWriter);
    }

    @Override
    public Number add(Sequence<Document> documents) throws IOException {
        List<Document> docs = documents.toList();
        writer.addDocuments(docs);
        return docs.size();
    }

    @Override
    public Number delete(Query query) throws IOException {
        int count = count(query);
        deleteNoCount(query);
        return count;
    }

    @Override
    public void deleteNoCount(Query query) throws IOException {
        writer.deleteDocuments(query);
    }

    @Override
    public void deleteAll() throws IOException {
        if (writer == null) return;

        writer.deleteAll();
        flush();
        deleteAllSegments(writer.getDirectory());
    }

    @Override
    public int count(final Query query) throws IOException {
        return search(new Callable1<Searcher, Integer>() {
            @Override
            public Integer call(Searcher searcher) throws Exception {
                return searcher.count(query);
            }
        });
    }

    @Override
    public <T> T search(Callable1<Searcher, T> callable) throws IOException {
        return using(searcher(), callable);
    }

    @Override
    public Searcher searcher() throws IOException {
        return pool.searcher();
    }

    @Override
    public CheckIndex.Status check() throws IOException {
        return new CheckIndex(writer.getDirectory()).checkIndex();
    }

    @Override
    public void fix() throws IOException {
        synchronized (lock) {
            CheckIndex checkIndex = new CheckIndex(writer.getDirectory());
            CheckIndex.Status status = checkIndex.checkIndex();
            checkIndex.fixIndex(status);
        }
    }

    @Override
    public void backup(final File folder) throws Exception {
        Files.delete(folder);
        IndexCommit indexCommit = null;
        SnapshotDeletionPolicy snapShotter = (SnapshotDeletionPolicy) writer.getConfig().getIndexDeletionPolicy();
        try {
            indexCommit = snapShotter.snapshot();
            using(directoryFor(folder), copy(indexCommit.getFileNames()).apply(writer.getDirectory()));
        } finally {
            if(indexCommit != null) snapShotter.release(indexCommit);
            writer.deleteUnusedFiles();
        }
    }

    private Directory directoryFor(File file) throws IOException {
        return new NIOFSDirectory(file);
    }

    public static Function2<Directory, Directory, Void> copy(final Collection<String> strings) {
        return new Function2<Directory, Directory, Void>() {
            @Override
            public Void call(Directory source, Directory destination) throws Exception {
                copy(source, destination, strings);
                return VOID;
            }
        };
    }

    public static void copy(Directory source, Directory destination, Collection<String> strings) throws IOException {
        for (String segment : strings) {
            source.copy(destination, segment, segment, IOContext.DEFAULT);
        }
    }

    @Override
    public void restore(File source) throws Exception {
        synchronized (lock) {
            deleteAll();
            Directory sourceDirectory = directoryFor(source);
            writer.addIndexes(sourceDirectory);
            flush();
        }
    }


    private void deleteAllSegments(Directory directory) throws IOException {
        for (String segment : directory.listAll()) {
            directory.deleteFile(segment);
        }
    }


    @Override
    public void close() throws IOException {
        writer = null;
        pool.close();
        try {
            ensureDirectoryUnlocked();
        } catch (Throwable ignored) {

        }
    }

    @Override
    public void flush() throws IOException {
        writer.commit();
        pool.markAsDirty();
    }

    private void ensureDirectoryUnlocked() throws IOException {
        if (IndexWriter.isLocked(writer.getDirectory())) {
            IndexWriter.unlock(writer.getDirectory());
        }
    }

}
