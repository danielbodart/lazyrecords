package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Callables;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.methods.indexWriter;
import static com.googlecode.totallylazy.Sequences.one;

public class IndexMigrator {
    public static void migrate(File old, File newStructure) throws IOException {
        final NIOFSDirectory oldDir = new NIOFSDirectory(old);
        final IndexWriter oldWriter = indexWriter(oldDir);
        final OptimisedStorage oldStorage = new OptimisedStorage(oldWriter);
        final NIOFSDirectory newDir = new NIOFSDirectory(newStructure);

        final NameToLuceneStorageFunction storageActivator = new ClosingNameToLuceneStorageFunction(new NameToLuceneDirectoryFunction(Callables.<String, Directory>ignoreAndReturn(newDir)), new KeywordAnalyzer());

        LucenePartitionedIndex partitionedIndex = new LucenePartitionedIndex(storageActivator);

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
        oldWriter.close();
        oldDir.close();
    }
}
