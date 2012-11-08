package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import org.junit.After;
import org.junit.Ignore;

import java.io.File;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.noSyncDirectory;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.partitionedIndex;
import static com.googlecode.totallylazy.Files.emptyTemporaryDirectory;

@Ignore
public class LucenePartitionedRecordsTest extends RecordsContract<LucenePartitionedRecords> {
    private File file;
    private PartitionedIndex partitionedIndex;

    @Override
    protected LucenePartitionedRecords createRecords() throws Exception {
        file = emptyTemporaryDirectory("totallylazy/partitioned-index");
        partitionedIndex = partitionedIndex(noSyncDirectory(file));
        return new LucenePartitionedRecords(partitionedIndex, new LuceneMappings(), logger);
    }

    @After
    public void cleanUp() throws Exception {
        super.cleanUp();
        records.close();
        partitionedIndex.close();
    }
}