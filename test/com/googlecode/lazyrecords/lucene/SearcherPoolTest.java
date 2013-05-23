package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.*;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

public class SearcherPoolTest {
    @Test
    public void returnsTheSameSearchIfNotWrites() throws Exception {
        OptimisedPool pool = new OptimisedPool(emptyDirectory());
        Searcher searcher1 = pool.searcher();
        assertThat(searcher1, is(notNullValue()));
        assertThat(pool.size(), is(1));

        Searcher searcher2 = pool.searcher();
        assertThat(searcher2, is(notNullValue()));
        assertThat(pool.size(), is(1));

        assertThat(searcher1, is(sameInstance(searcher2)));
    }

    private RAMDirectory emptyDirectory() throws IOException {
        RAMDirectory ramDirectory = new RAMDirectory();
        IndexWriter writer = new IndexWriter(ramDirectory, new IndexWriterConfig(Version.LUCENE_30, new KeywordAnalyzer()));
        writer.commit();
        return ramDirectory;
    }

    @Test
    public void closeSearcherWhenDirty() throws Exception {
        OptimisedPool pool = new OptimisedPool(emptyDirectory());
        Searcher searcher1 = pool.searcher();
        assertThat(pool.size(), is(1));
        searcher1.close();
        assertThat(pool.size(), is(1));
        pool.markAsDirty();
        assertThat(pool.size(), is(0));
    }

    @Test
    public void closeDirtyCheckedOutSearcherWhenTheyAreReturned() throws Exception {
        OptimisedPool pool = new OptimisedPool(emptyDirectory());
        Searcher searcher1 = pool.searcher();
        Searcher searcher2 = pool.searcher();
        assertThat(pool.size(), is(1));
        searcher1.close();
        assertThat(pool.size(), is(1));

        pool.markAsDirty();

        assertThat(pool.size(), is(1));
        searcher2.close();
        assertThat(pool.size(), is(0));
    }

    @Test
    public void onlyClosesCheckedInPoolsWhenEveryoneHasFinished() throws Exception {
        OptimisedPool pool = new OptimisedPool(emptyDirectory());
        Searcher searcher1 = pool.searcher();
        Searcher searcher2 = pool.searcher();
        assertThat(pool.size(), is(1));
        pool.markAsDirty();
        assertThat(pool.size(), is(1));
        searcher1.close();
        assertThat(pool.size(), is(1));
        searcher2.close();
        assertThat(pool.size(), is(0));
    }
}
