package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.collections.CloseableList;
import com.googlecode.totallylazy.Sequences;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.methods.indexWriter;
import static com.googlecode.totallylazy.Files.files;
import static com.googlecode.totallylazy.Files.isDirectory;
import static java.lang.String.format;

public class IndexAnalyzerMigrator {

    public static void migrate(File oldDirectory, File newDirectory, Analyzer newAnalyzer) throws IOException {

        try (
            NIOFSDirectory oldDir = new NIOFSDirectory(oldDirectory);
            NIOFSDirectory newDir = new NIOFSDirectory(newDirectory);

            IndexWriter oldWriter = indexWriter(oldDir);
            IndexWriter newWriter = indexWriter(newDir, newAnalyzer);

            OptimisedStorage oldStorage = new OptimisedStorage(oldWriter);
            OptimisedStorage newStorage = new OptimisedStorage(newWriter);

            Searcher oldSearcher = oldStorage.searcher()
        ) {
            ScoreDoc[] docs = oldSearcher.search(Lucene.all(), Lucene.NO_SORT).scoreDocs;
            for (ScoreDoc doc : docs) {
                Document document = oldSearcher.document(doc.doc);
                newStorage.add(Sequences.one(document));
            }
            newStorage.flush();
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
