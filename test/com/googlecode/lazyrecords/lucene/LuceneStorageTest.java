package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.time.SystemClock;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LuceneStorageTest {
    @Before
    public void forceStale() {
        System.setProperty(SimplePool.StaleSearcherSeconds.PROPERTY, "0");
    }

    @Test
    public void canSearchAnEmptyIndex() throws Exception {
        LuceneStorage storage = new OptimisedStorage(new RAMDirectory(), new SystemClock());
        assertThat(storage.count(new MatchAllDocsQuery()), is(0));
    }

    @Test
    public void canDeleteAll() throws IOException {
        LuceneStorage storage = new OptimisedStorage(new RAMDirectory(), new SystemClock());
        storage.add(sequence(new Document()));
        storage.add(sequence(new Document()));
        assertThat(storage.count(new MatchAllDocsQuery()), is(2));
        storage.deleteAll();
        assertThat(storage.count(new MatchAllDocsQuery()), is(0));
    }
}
