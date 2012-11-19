package com.googlecode.lazyrecords.simpledb;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.MemoryLogger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SchemaBasedRecordContract;
import com.googlecode.lazyrecords.simpledb.mappings.SimpleDBMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.Matchers;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore
public class SimpleDBRecordsTest extends SchemaBasedRecordContract<SimpleDBRecords> {
    private AmazonSimpleDBClient amazonSimpleDBClient;

    @Override
    protected SimpleDBRecords createRecords() throws Exception {
        InputStream credentials = getClass().getResourceAsStream("AwsCredentials.properties");
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        amazonSimpleDBClient = new AmazonSimpleDBClient(new PropertiesCredentials(credentials), new ClientConfiguration().withMaxErrorRetry(5));
        schema = new SimpleDBSchema(amazonSimpleDBClient);
        return simpleDbRecords(logger);
    }

    private SimpleDBRecords simpleDbRecords(Logger logger1) {
        return new SimpleDBRecords(amazonSimpleDBClient, true, new SimpleDBMappings(), logger1, schema);
    }

	@Override
	@Ignore("Still thinking about lexical representation of BigDecimal")
	public void supportsBigDecimal() throws Exception {
	}

	@Override
	@Ignore("Not implemented yet")
	public void supportsSortingByMultipleKeywords() throws Exception {
	}

	@Override
    @Ignore("Not Supported by AWS")
    public void supportsAliasingAKeyword() throws Exception {
    }

    @Test
    public void canAddMoreThat25RecordsAtATimeAndReceiveMoreThanAHundred() throws Exception {
        assertThat(records.get(books).size(), is(3));
        Sequence<Record> newBooks = repeat(Record.constructors.record().set(isbn, zenIsbn)).take(100);
        assertThat(newBooks.size(), NumberMatcher.is(100));
        records.add(books, newBooks);
        assertThat(records.get(books).size(), is(103));
    }

    @Test
    public void memorisesAndThereforeOnlyExecutesSqlOnce() throws Exception {
        MemoryLogger logger = new MemoryLogger();
        Sequence<Record> result = simpleDbRecords(logger).get(people).sortBy(age);
        Record head = result.head();
        Sequence<Map<String, ?>> logs = logger.data();
        assertThat(head, Matchers.is(result.head())); // Check iterator
        assertThat(logs, Matchers.is(logger.data())); // Check queries
    }
}
