package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Sequences;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.isDirectory;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class IndexAnalyzerMigrator {

    public static void migrate(File oldDirectory, File newDirectory, Analyzer newAnalyzer) throws IOException {
        final CloseableList closeables = new CloseableList();

        try {
            NIOFSDirectory oldDir = closeables.manage(new NIOFSDirectory(oldDirectory));
            NIOFSDirectory newDir = closeables.manage(new NIOFSDirectory(newDirectory));

            OptimisedStorage oldStorage = closeables.manage(new OptimisedStorage(oldDir, new LucenePool(oldDir)));
            OptimisedStorage newStorage = closeables.manage(new OptimisedStorage(newDir, Version.LUCENE_4_10_0, newAnalyzer, IndexWriterConfig.OpenMode.CREATE_OR_APPEND, new LucenePool(oldDir)));

            Searcher oldSearcher = closeables.manage(oldStorage.searcher());
            ScoreDoc[] docs = oldSearcher.search(Lucene.all(), Lucene.NO_SORT).scoreDocs;
            for (ScoreDoc doc : docs) {
                Document document = oldSearcher.document(doc.doc);
                newStorage.add(Sequences.one(document));
            }
            newStorage.flush();
        } finally {
            closeables.close();
        }
    }

    public static void migrateShardedIndex(final File oldDirectory, final File newDirectory, final Analyzer newAnalyzer) {
        files(oldDirectory).filter(isDirectory()).forEach(new Block<File>() {
            @Override
            protected void execute(File oldShard) throws Exception {
                final File newShard = new File(format("%s/%s", newDirectory, oldShard.getName()));
                migrate(oldShard, newShard, newAnalyzer);
            }
        });
    }
}
