package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.collections.PersistentMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

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

    public static class methods {
        public static IndexWriter indexWriter(Directory directory) {
            return indexWriter(directory, new KeywordAnalyzer());
        }

        public static IndexWriter indexWriter(Directory directory, Analyzer analyzer) {
            final IndexWriter writer;
            try {
                writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND).setIndexDeletionPolicy(new SnapshotDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy())));
                writer.commit();
                return writer;
            } catch (IOException e) {
                throw LazyException.lazyException(e);
            }
        }
    }
}
