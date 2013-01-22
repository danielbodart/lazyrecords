package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.one;

public class IndexMigrator {
    public static void migrate(File old, File newStructure) throws IOException {
        NIOFSDirectory oldDir = new NIOFSDirectory(old);
        OptimisedStorage oldStorage = new OptimisedStorage(oldDir, new LucenePool(oldDir));

        LucenePartitionedIndex partitionedIndex = LucenePartitionedIndex.partitionedIndex(newStructure);

        Searcher oldSeacher = oldStorage.searcher();
        ScoreDoc[] docs = oldSeacher.search(Lucene.all(), Lucene.NO_SORT).scoreDocs;
        for (ScoreDoc doc : docs) {
            Document document = oldSeacher.document(doc.doc);
            String type = document.get(Lucene.RECORD_KEY.name());
            partitionedIndex.partition(type).add(one(document));
        }

        partitionedIndex.close();
        oldSeacher.close();
        oldStorage.close();
        oldDir.close();
    }
}
