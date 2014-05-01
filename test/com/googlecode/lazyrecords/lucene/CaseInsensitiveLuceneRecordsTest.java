package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.Matchers;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.lazyrecords.Grammar.is;
import static com.googlecode.lazyrecords.Grammar.keyword;
import static com.googlecode.lazyrecords.Grammar.record;
import static com.googlecode.lazyrecords.Grammar.where;
import static com.googlecode.lazyrecords.RecordsContract.Books.isbn;
import static com.googlecode.lazyrecords.RecordsContract.People.age;
import static com.googlecode.lazyrecords.RecordsContract.People.dob;
import static com.googlecode.lazyrecords.RecordsContract.People.firstName;
import static com.googlecode.lazyrecords.RecordsContract.People.lastName;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.totallylazy.Files.emptyVMDirectory;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;

public class CaseInsensitiveLuceneRecordsTest extends RecordsContract<LuceneRecords> {

    private static final LogicalPredicate<Record> bobPredicate = where(keyword("firstName", String.class), is("bOB"));

    private LuceneStorage storage;
    private Directory directory;
    private Lucene lucene;

    @Override
    protected LuceneRecords createRecords() throws Exception {
        directory = new NoSyncDirectory(emptyVMDirectory("case-insensitive-lucene"));
        storage = CaseInsensitive.storage(directory, new LucenePool(directory));
        lucene = new Lucene(new StringMappings());
        return luceneRecords(logger);
    }

    private LuceneRecords luceneRecords(Logger logger1) throws IOException {
        return new LuceneRecords(storage, new LuceneMappings(), logger1, CaseInsensitive.luceneQueryPreprocessor());
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
        Sequence<Record> result = records.get(people).filter(bobPredicate);
        assertThat(result.size(), Matchers.is(1));
        assertThat(result.head().get(firstName), Matchers.is("Bob"));
    }

    @Test
    public void deleteShouldBeCaseInsensitive() throws Exception {
        Integer removedCount = records.remove(people, bobPredicate).intValue();
        assertThat(removedCount, Matchers.is(1));
        assertThat(records.get(people).filter(bobPredicate).size(), Matchers.is(0));
    }

    @Test
    public void countShouldBeCaseInsensitive() throws Exception {
        Integer count = records.count(lucene.query(bobPredicate));
        assertThat(count, Matchers.is(1));
    }

    @Test
    public void updateShouldBeCaseInsensitive() throws Exception {
        final Record updatedRecord = record(firstName, "Bob", lastName, "ChangedLastName", age, 11, dob, date(1976, 1, 10), isbn, cleanCode);
        Integer updatedCount = records.put(people, pair(bobPredicate, updatedRecord)).intValue();
        assertThat(updatedCount, Matchers.is(1));
        assertThat(records.get(people).filter(bobPredicate).head().get(lastName), Matchers.is("ChangedLastName"));
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
