package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.Matchers;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.lazyrecords.Grammar.is;
import static com.googlecode.lazyrecords.Grammar.keyword;
import static com.googlecode.lazyrecords.Grammar.where;
import static com.googlecode.lazyrecords.RecordsContract.People.firstName;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.totallylazy.Files.emptyVMDirectory;
import static org.hamcrest.MatcherAssert.assertThat;

public class CaseInsensitiveLuceneRecordsTest extends RecordsContract<LuceneRecords> {

    private LuceneStorage storage;
    private Directory directory;

    @Override
    protected LuceneRecords createRecords() throws Exception {
        directory = new NoSyncDirectory(emptyVMDirectory("case-insensitive-lucene"));
        storage = CaseInsensitive.storage(directory, new LucenePool(directory));
        return luceneRecords(logger);
    }

    private LuceneRecords luceneRecords(Logger logger1) throws IOException {
        return new LuceneRecords(storage, new LuceneMappings(), logger1, CaseInsensitive.queryVisitor());
    }

    @After
    public void cleanUp() throws Exception {
        super.cleanUp();
        records.close();
        storage.close();
        directory.close();
    }


    @Test
    public void searchShouldBeCaseInsensitive() throws Exception {
        Sequence<Record> result = records.get(people).filter(where(keyword("firstName", String.class), is("bOB")));
        assertThat(result.size(), Matchers.is(1));
        assertThat(result.head().get(firstName), Matchers.is("Bob"));
    }

    @Override
    @Ignore()
    public void supportsBigDecimal() throws Exception {
    }


    @Override
    @Ignore
    public void supportsConcatenationDuringFiltering() throws Exception {
    }

    @Override
    @Ignore
    public void supportsAliasingAKeywordDuringFilter() throws Exception {
    }

    @Override
    @Ignore
    public void canFullyQualifyAKeywordDuringFiltering() throws Exception {
    }

}
