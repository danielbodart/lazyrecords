package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Sequences;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OptimizedStorageTest {
    @Test
    public void canBackupAndRestore() throws Exception {
        LuceneStorage source = storage();
        source.add(Sequences.<Document>sequence(new Document()));
        source.flush();
        assertThat(source.count(Lucene.all()), is(1));

        File backup = Files.temporaryFile();
        source.backup(backup);

        LuceneStorage destination = storage();
        destination.restore(backup);

        assertThat(destination.count(Lucene.all()), is(1));
    }

    @Test
    public void canSearchAnEmptyIndex() throws Exception {
        LuceneStorage storage = storage();
        assertThat(storage.count(Lucene.all()), is(0));
    }

    @Test
    public void canDeleteAll() throws IOException {
        LuceneStorage storage = storage();
        storage.add(Sequences.<Document>sequence(new Document()));
        storage.add(Sequences.<Document>sequence(new Document()));
        storage.flush();
        assertThat(storage.count(Lucene.all()), is(2));
        storage.deleteAll();
        assertThat(storage.count(Lucene.all()), is(0));
    }

    private LuceneStorage storage() throws IOException {
        RAMDirectory directory = new RAMDirectory();
        return new OptimisedStorage(directory, new LucenePool(directory));
    }
}