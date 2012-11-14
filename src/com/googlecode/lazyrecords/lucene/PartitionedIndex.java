package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Lazy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.nioDirectory;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.ramDirectory;
import static com.googlecode.totallylazy.Callables.value;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Zip.unzip;
import static com.googlecode.totallylazy.Zip.zip;

public class PartitionedIndex implements Closeable, Persistence {
    private final ConcurrentMap<String, Lazy<LuceneStorage>> partitions = new ConcurrentHashMap<String, Lazy<LuceneStorage>>();
    private final CloseableList closeables = new CloseableList();
    private final Function1<String, Directory> directoryActivator;

    private PartitionedIndex(Function1<String, Directory> directoryActivator) {
        this.directoryActivator = directoryActivator;
    }

    public static PartitionedIndex partitionedIndex(final File rootDirectory) {
        return partitionedIndex(nioDirectory(rootDirectory));
    }


    public static PartitionedIndex partitionedIndex() {
        return partitionedIndex(ramDirectory());
    }

    public static PartitionedIndex partitionedIndex(Function1<String, Directory> directoryActivator) {
        return new PartitionedIndex(directoryActivator);
    }

    public void close() throws IOException {
        sequence(partitions.values()).map(value(LuceneStorage.class)).each(safeClose());
        partitions.clear();
        closeables.close();
    }

    LuceneStorage partition(Definition definition) throws IOException {
        return partition(definition.name());
    }

    LuceneStorage partition(final String definition) throws IOException {
        partitions.putIfAbsent(definition, lazyStorage(definition));
        return partitions.get(definition).value();
    }

    private Lazy<LuceneStorage> lazyStorage(final String definition) {
        return new Lazy<LuceneStorage>() {
            @Override
            protected LuceneStorage get() throws Exception {
                Directory directory = closeables.manage(directoryActivator.call(definition));
                SearcherPool searcherPool = closeables.manage(new LucenePool(directory));
                return new OptimisedStorage(directory, searcherPool);
            }
        };
    }

    @Override
    public void deleteAll() throws IOException {
        for (Lazy<LuceneStorage> storageLazy : partitions.values()) {
            storageLazy.value().deleteAll();
        }
        close();
    }

    @Override
    public void backup(File bgb) throws Exception {
        File destination = tempUnzipLocation();
        Files.delete(destination);

        for (Map.Entry<String, Lazy<LuceneStorage>> entry : partitions.entrySet()) {
            String name = entry.getKey();
            LuceneStorage luceneStorage = entry.getValue().value();
            luceneStorage.backup(directory(destination, name));
        }

        zip(destination, bgb);
        Files.delete(destination);
    }

    @Override
    public void restore(File file) throws Exception {
        File sourceDirectory = unzipIfNeeded(file);
        deleteAll();

        for (File partition : Files.files(sourceDirectory)) {
            String name = partition.getName();
            LuceneStorage luceneStorage = partition(name);
            luceneStorage.restore(directory(sourceDirectory, name));
        }
    }

    private File tempUnzipLocation() {
        return Files.emptyTemporaryDirectory("lucene-index-unzipped");
    }

    private File unzipIfNeeded(File source) throws IOException {
        if (source.isFile()) {
            File unzipped = tempUnzipLocation();
            unzip(source, unzipped);
            return unzipped;
        }
        return source;
    }


    public static class functions {
        public static Function1<String, Directory> ramDirectory() {
            return new Function1<String, Directory>() {
                @Override
                public Directory call(String notRequired) throws Exception {
                    return new RAMDirectory();
                }
            };
        }

        public static Function1<String, Directory> noSyncDirectory(final File rootDirectory) {
            return new Function1<String, Directory>() {
                @Override
                public Directory call(String definition) throws Exception {
                    return new NoSyncDirectory(Files.directory(rootDirectory, definition));
                }
            };
        }

        public static Function1<String, Directory> nioDirectory(final File rootDirectory) {
            return new Function1<String, Directory>() {
                @Override
                public Directory call(String definition) throws Exception {
                    return new NIOFSDirectory(Files.directory(rootDirectory, definition));
                }
            };
        }

    }
}