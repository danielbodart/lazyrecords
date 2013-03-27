package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import org.junit.After;
import org.junit.Ignore;

import java.io.File;

import static com.googlecode.lazyrecords.lucene.LucenePartitionedIndex.partitionedIndex;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.noSyncDirectory;
import static com.googlecode.totallylazy.Files.emptyRandomDirectory;

public class LucenePartitionedRecordsTest extends RecordsContract<LucenePartitionedRecords> {
    private File file;
    private LucenePartitionedIndex partitionedIndex;

    @Override
    protected LucenePartitionedRecords createRecords() throws Exception {
        file = emptyRandomDirectory("totallylazy/partitioned-index");
        partitionedIndex = partitionedIndex(noSyncDirectory(file));
        return new LucenePartitionedRecords(partitionedIndex, new LuceneMappings(), logger);
    }

    @After
    public void cleanUp() throws Exception {
        super.cleanUp();
        records.close();
        partitionedIndex.close();
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