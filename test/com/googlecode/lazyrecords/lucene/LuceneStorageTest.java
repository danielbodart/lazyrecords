package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LuceneStorageTest {
    @Test
    public void canSearchAnEmptyIndex() throws Exception {
        LuceneStorage storage = storage();
        assertThat(storage.count(new MatchAllDocsQuery()), is(0));
    }

    @Test
    public void canDeleteAll() throws IOException {
        LuceneStorage storage = storage();
        storage.add(sequence(new Document()));
        storage.add(sequence(new Document()));
        assertThat(storage.count(new MatchAllDocsQuery()), is(2));
        storage.deleteAll();
        assertThat(storage.count(new MatchAllDocsQuery()), is(0));
    }

    private LuceneStorage storage() throws IOException {
        RAMDirectory directory = new RAMDirectory();
        return new OptimisedStorage(directory, new LucenePool(directory));
    }
}