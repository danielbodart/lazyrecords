package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Files;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LuceneStorageTest {
    @Test
    public void canBackupAndRestore() throws Exception {
        LuceneStorage source = storage();
        source.add(sequence(new Document()));
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
        storage.add(sequence(new Document()));
        storage.add(sequence(new Document()));
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