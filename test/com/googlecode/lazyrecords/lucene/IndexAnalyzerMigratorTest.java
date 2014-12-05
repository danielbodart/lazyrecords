package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Grammar.where;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.lucene.IndexAnalyzerMigrator.migrate;
import static com.googlecode.lazyrecords.lucene.IndexAnalyzerMigrator.migrateShardedIndex;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.mmapDirectory;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.noSyncDirectory;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.methods.indexWriter;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Files.emptyVMDirectory;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class IndexAnalyzerMigratorTest {

    private static final Keyword<String> FIELD = keyword("caseSensitiveFieldName", String.class);
    private static final Definition FIRST_DEFINITION = definition("first_shard", FIELD);
    private static final Definition SECOND_DEFINITION = definition("second_shard", FIELD);
    private static final Sequence<Record> SHARD_DATA = sequence(record(FIELD, "CaSeSeNsItIvE"), record(FIELD, "lowercase"));

    private File indexDirectory;

    @Before
    public void setupCaseSensitiveTestIndex() throws IOException {
        indexDirectory = emptyVMDirectory("lucene-records-case-sensitive");
        final NameToLuceneDirectoryFunction directoryActivator = new NameToLuceneDirectoryFunction(noSyncDirectory(indexDirectory));
        final ClosingNameToLuceneStorageFunction storageActivator = new ClosingNameToLuceneStorageFunction(directoryActivator, new KeywordAnalyzer());
        LucenePartitionedIndex partitionedIndex = new LucenePartitionedIndex(storageActivator);
        createTestShard(partitionedIndex, FIRST_DEFINITION);
        createTestShard(partitionedIndex, SECOND_DEFINITION);
        safeClose(partitionedIndex);
        safeClose(storageActivator);
    }

    private void createTestShard(LucenePartitionedIndex index, Definition definition) throws IOException {
        final LuceneStorage shard = index.partition(definition);
        final LuceneRecords records = new LuceneRecords(shard);
        records.add(definition, SHARD_DATA);
        safeClose(records, shard);
    }

    @Test
    public void shouldMigrateAnIndexWithTheSpecifiedAnalyzer() throws Exception {
        final File newIndex = emptyVMDirectory("lucene-records-migrated-single");
        final File oldIndex = new File(format("%s/%s", indexDirectory.getAbsolutePath(), FIRST_DEFINITION.name()));
        migrate(oldIndex, newIndex, new CaseInsensitive.StringPhraseAnalyzer());
        final NoSyncDirectory newDirectory = new NoSyncDirectory(newIndex);

        final OptimisedStorage newStorage = new OptimisedStorage(indexWriter(newDirectory, CaseInsensitive.queryAnalyzer()));
        assertShardHasBeenMigratedCorrectly(newStorage, FIRST_DEFINITION);

        safeClose(newDirectory);
    }

    @Test
    public void shouldMigrateAShardedIndex() throws Exception {
        final File newIndex = emptyVMDirectory("lucene-records-migrated-sharded");
        migrateShardedIndex(indexDirectory, newIndex, new CaseInsensitive.StringPhraseAnalyzer());
        final NameToLuceneDirectoryFunction directoryActivator = new NameToLuceneDirectoryFunction(mmapDirectory(newIndex));
        final ClosingNameToLuceneStorageFunction storageActivator = new ClosingNameToLuceneStorageFunction(directoryActivator, new KeywordAnalyzer());
        final LucenePartitionedIndex migratedIndex = new LucenePartitionedIndex(storageActivator);

        assertShardHasBeenMigratedCorrectly(migratedIndex.partition(FIRST_DEFINITION), FIRST_DEFINITION);
        assertShardHasBeenMigratedCorrectly(migratedIndex.partition(SECOND_DEFINITION), SECOND_DEFINITION);

        safeClose(migratedIndex);
    }

    public void assertShardHasBeenMigratedCorrectly(LuceneStorage storage, Definition definition) throws IOException {
        final LuceneRecords records = new LuceneRecords(storage, new LuceneMappings(), new IgnoreLogger(), CaseInsensitive.luceneQueryPreprocessor());

        assertThat(records.get(definition).size(), is(SHARD_DATA.size()));
        assertThat(records.get(definition).filter(where(FIELD, Grammar.is("lowercase"))).map(FIELD), contains("lowercase"));
        assertThat(records.get(definition).filter(where(FIELD, Grammar.is("casesensitive"))).map(FIELD), contains("CaSeSeNsItIvE"));

        safeClose(records, storage);
    }
}
