package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.matchers.Matchers;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.googlecode.lazyrecords.Grammar.*;
import static com.googlecode.lazyrecords.RecordsContract.People.*;
import static com.googlecode.totallylazy.Files.emptyVMDirectory;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class LuceneRecordsTest extends RecordsContract<LuceneRecords> {
    public static final Version VERSION = Version.LUCENE_45;
    public static final Analyzer ANALYZER = new StandardAnalyzer(VERSION);
    private Directory directory;
    private LuceneStorage storage;
    private File file;

    @Override
    protected LuceneRecords createRecords() throws Exception {
        file = emptyVMDirectory("lucene-records");
        directory = new NoSyncDirectory(file);
        storage = new OptimisedStorage(directory, new LucenePool(directory));
        return luceneRecords(logger);
    }

    private LuceneRecords luceneRecords(Logger logger1) throws IOException {
        return new LuceneRecords(storage, new LuceneMappings(), logger1);
    }

    @After
    public void cleanUp() throws Exception {
        super.cleanUp();
        records.close();
        storage.close();
        directory.close();
    }

    @Override
    @Ignore("Still thinking about lexical representation of BigDecimal")
    public void supportsBigDecimal() throws Exception {
    }

    @Test
    public void canQueryIndexDirectly() throws Exception {
        QueryParser parser = new QueryParser(VERSION, null, ANALYZER);
        Sequence<Record> results = records.query(parser.parse("type:people +firstName:da*"), Sequences.<Keyword<?>>sequence(lastName));
        assertThat(results.map(lastName), hasExactly("bodart"));
    }

    @Test
    public void memorisesAndThereforeOnlyExecutesQueryOnce() throws Exception {
        Sequence<Record> result = records.get(people).sortBy(age);
        Record head = result.head();
        Sequence<Map<String, ?>> logs = memory.data();
        assertThat(head, Matchers.is(result.head())); // Check iterator
        assertThat(memory.data(), Matchers.is(logs)); // Check queries
    }

    @Override
    @Ignore
    public void supportsConcatenationDuringFiltering() throws Exception {
    }

    @Test
    public void supportsGettingWithAliasesInDefinition() throws Exception {
        final AliasedKeyword<String> alias = people.firstName.as("name");
        final Definition definition = Definition.constructors.definition(people.name(), alias, people.lastName);
        final Record record = records.get(definition).filter(where(people.lastName, is("bodart"))).head();
        assertThat(record.get(alias), Matchers.is("dan"));
    }

    @Test
    @Ignore("This won't work because Keyword.call(record) doesn't work when the Keyword is aliased and present in the definition. Note this is why we currently call map() with a non-aliased Keyword")
    public void supportsSortingByAliases() throws Exception {
        final AliasedKeyword<String> alias = people.firstName.as("name");
        final Definition definition = Definition.constructors.definition(people.name(), alias, people.lastName);
        final Sequence<String> sortedRecords = records.get(definition).sortBy(descending(alias)).map(keyword("name", String.class));
        assertThat(sortedRecords, Matchers.is(sequence("matt", "dan", "Bob")));
    }
}
