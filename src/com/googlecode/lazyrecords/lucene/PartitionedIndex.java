package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.collections.PersistentMap;
import com.googlecode.totallylazy.collections.PersistentSortedMap;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;

public interface PartitionedIndex extends Persistence {
    LuceneStorage partition(Definition definition) throws IOException;

    LuceneStorage partition(String definition) throws IOException;

    PersistentMap<String, LuceneStorage> partitions();

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

        public static Function1<String, Directory> mmapDirectory(final File rootDirectory) {
            return new Function1<String, Directory>() {
                @Override
                public Directory call(String definition) throws Exception {
                    return new MMapDirectory(Files.directory(rootDirectory, definition));
                }
            };
        }
    }
}
