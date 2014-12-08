package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.collections.PersistentMap;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Files.directory;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Zip.unzip;
import static com.googlecode.totallylazy.Zip.zip;
import static com.googlecode.totallylazy.collections.PersistentSortedMap.constructors.sortedMap;

public class LucenePartitionedIndex implements Closeable, Persistence, PartitionedIndex {
    private final ConcurrentMap<String, LuceneStorage> partitions = new ConcurrentHashMap<>();
    private final NameToLuceneStorageFunction storageActivator;

    public LucenePartitionedIndex(NameToLuceneStorageFunction storageActivator) {
        this.storageActivator = storageActivator;
    }

    public void close() throws IOException {
        sequence(partitions.values()).each(safeClose());
        partitions.clear();
    }

    @Override
    public LuceneStorage partition(Definition definition) throws IOException {
        return partition(definition.name());
    }

    @Override
    public LuceneStorage partition(final String definition) throws IOException {
        if (!partitions.containsKey(definition)) {
            partitions.putIfAbsent(definition, storageActivator.getForName(definition));
        }
        return partitions.get(definition);
    }

    @Override
    public PersistentMap<String, LuceneStorage> partitions() {
        return sortedMap(Maps.pairs(partitions));
    }

    @Override
    public void deleteAll() throws IOException {
        for (LuceneStorage partition : partitions.values()) {
            partition.deleteAll();
        }
    }

    @Override
    public void backup(File bgb) throws Exception {
        File destination = tempUnzipLocation();
        Files.delete(destination);

        for (Map.Entry<String, LuceneStorage> entry : partitions.entrySet()) {
            String name = entry.getKey();
            LuceneStorage luceneStorage = entry.getValue();
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
        return Files.emptyVMDirectory("lucene-index-unzipped");
    }

    private File unzipIfNeeded(File source) throws IOException {
        if (source.isFile()) {
            File unzipped = tempUnzipLocation();
            unzip(source, unzipped);
            return unzipped;
        }
        return source;
    }
}