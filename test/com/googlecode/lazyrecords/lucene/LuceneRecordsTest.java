package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static com.googlecode.totallylazy.Files.temporaryDirectory;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class LuceneRecordsTest extends RecordsContract<LuceneRecords> {
    public static final Version VERSION = Version.LUCENE_33;
    public static final Analyzer ANALYZER = new StandardAnalyzer(VERSION);
    private Directory directory;
    private LuceneStorage storage;
    private File file;

    @Override
    protected LuceneRecords createRecords() throws Exception {
        file = temporaryDirectory("totallylazy");
        directory = new NoSyncDirectory(file);
        storage = new OptimisedStorage(directory, new LucenePool(directory));
        return new LuceneRecords(storage, new LuceneMappings(), logger);
    }

    @After
    public void cleanUp() throws Exception {
        super.cleanUp();
        records.close();
        storage.close();
        directory.close();
    }

    @Test
    public void canQueryIndexDirectly() throws Exception {
        QueryParser parser = new QueryParser(VERSION, null, ANALYZER);
        Sequence<Record> results = records.query(parser.parse("type:people +firstName:da*"), Sequences.<Keyword<?>>sequence(lastName));
        assertThat(results.map(lastName), hasExactly("bodart"));
    }
}
