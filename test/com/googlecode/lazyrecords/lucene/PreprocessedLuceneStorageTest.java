package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.methods.indexWriter;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class PreprocessedLuceneStorageTest {

    @Test
    public void testDelete() throws Exception {
        final LuceneQueryPreprocessorSpy queryPreprocessorSpy = new LuceneQueryPreprocessorSpy();
        PreprocessedLuceneStorage preprocessedLuceneStorage = new PreprocessedLuceneStorage(storage(), queryPreprocessorSpy);
        preprocessedLuceneStorage.delete(new TermQuery(new Term("field", "value")));
        assertThat(queryPreprocessorSpy.processInvocationCount, is(1));
    }

    @Test
    public void testDeleteNoCount() throws Exception {
        final LuceneQueryPreprocessorSpy queryPreprocessorSpy = new LuceneQueryPreprocessorSpy();
        PreprocessedLuceneStorage preprocessedLuceneStorage = new PreprocessedLuceneStorage(storage(), queryPreprocessorSpy);
        preprocessedLuceneStorage.deleteNoCount(new TermQuery(new Term("field", "value")));
        assertThat(queryPreprocessorSpy.processInvocationCount, is(1));
    }

    @Test
    public void testCount() throws Exception {
        final LuceneQueryPreprocessorSpy queryPreprocessorSpy = new LuceneQueryPreprocessorSpy();
        PreprocessedLuceneStorage preprocessedLuceneStorage = new PreprocessedLuceneStorage(storage(), queryPreprocessorSpy);
        preprocessedLuceneStorage.count(new TermQuery(new Term("field", "value")));
        assertThat(queryPreprocessorSpy.processInvocationCount, is(1));
    }

    private LuceneStorage storage() throws IOException {
        RAMDirectory directory = new RAMDirectory();
        return new OptimisedStorage(indexWriter(directory));
    }

    private static class LuceneQueryPreprocessorSpy extends DoNothingLuceneQueryPreprocessor {
        public int processInvocationCount = 0;

        @Override
        public Query process(TermQuery query) {
            processInvocationCount++;
            return query;
        }
    }
}