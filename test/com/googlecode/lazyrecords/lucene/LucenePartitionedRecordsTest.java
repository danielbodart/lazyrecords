package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.junit.After;
import org.junit.Ignore;

import java.io.File;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.noSyncDirectory;
import static com.googlecode.totallylazy.Files.emptyVMDirectory;

public class LucenePartitionedRecordsTest extends RecordsContract<LucenePartitionedRecords> {
    private File file;
    private LucenePartitionedIndex partitionedIndex;
    private ClosingNameToLuceneStorageFunction storageActivator;

    @Override
    protected LucenePartitionedRecords createRecords() throws Exception {
        file = emptyVMDirectory("totallylazy/partitioned-index");
        final NameToLuceneDirectoryFunction directoryActivator = new NameToLuceneDirectoryFunction(noSyncDirectory(file));
        storageActivator = new ClosingNameToLuceneStorageFunction(directoryActivator, new KeywordAnalyzer());
        partitionedIndex = new LucenePartitionedIndex(storageActivator);
        return new LucenePartitionedRecords(partitionedIndex, new LuceneMappings(), logger);
    }

    @After
    public void cleanUp() throws Exception {
        super.cleanUp();
        records.close();
        partitionedIndex.close();
        storageActivator.close();
    }

    @Override
    @Ignore("Still thinking about lexical representation of BigDecimal")
    public void supportsBigDecimal() throws Exception {
    }

    @Override
    @Ignore
    public void supportsConcatenationDuringFiltering() throws Exception {
    }

    @Override
    @Ignore
    public void supportsAliasingAKeywordDuringFilter() throws Exception {
    }

    @Override
    @Ignore
    public void canFullyQualifyAKeywordDuringFiltering() throws Exception {
    }
}