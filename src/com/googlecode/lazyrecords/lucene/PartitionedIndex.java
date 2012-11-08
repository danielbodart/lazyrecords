package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.lucene.mappings.BackgroundStorage;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.nioDirectory;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.ramDirectory;
import static com.googlecode.totallylazy.Callables.value;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Sequences.sequence;

public class PartitionedIndex implements Closeable {
    private final ConcurrentHashMap<String, Lazy<LuceneStorage>> partitions = new ConcurrentHashMap<String, Lazy<LuceneStorage>>();
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
                return new BackgroundStorage(new OptimisedStorage(directory, new LucenePool(directory)));
            }
        };
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
