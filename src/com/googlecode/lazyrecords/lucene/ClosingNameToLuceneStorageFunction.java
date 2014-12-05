package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.CloseableList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import java.io.IOException;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.methods.indexWriter;

public class ClosingNameToLuceneStorageFunction implements NameToLuceneStorageFunction {
    private Analyzer analyzer;
    private NameToLuceneDirectoryFunction directoryActivator;

    private CloseableList closeables = new CloseableList();

    public ClosingNameToLuceneStorageFunction(NameToLuceneDirectoryFunction directoryActivator, Analyzer analyzer) {
        this.directoryActivator = directoryActivator;
        this.analyzer = analyzer;
    }

    @Override
    public LuceneStorage getForName(String name) {
        final Directory directory = closeables.manage(directoryActivator.value().apply(name));
        final IndexWriter indexWriter = closeables.manage(indexWriter(directory, analyzer));
        return closeables.manage(new OptimisedStorage(indexWriter));
    }

    @Override
    public void close() throws IOException {
        closeables.close();
    }
}
