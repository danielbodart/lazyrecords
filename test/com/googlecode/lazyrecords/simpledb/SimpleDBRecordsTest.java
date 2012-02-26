package com.googlecode.lazyrecords.simpledb;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SchemaBasedRecordContract;
import com.googlecode.lazyrecords.mappings.Mappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("Manual Test")
public class SimpleDBRecordsTest extends SchemaBasedRecordContract<SimpleDBRecords> {
    @Override
    protected SimpleDBRecords createRecords() throws Exception {
        InputStream credentials = getClass().getResourceAsStream("AwsCredentials.properties");
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        final AmazonSimpleDBClient amazonSimpleDBClient = new AmazonSimpleDBClient(new PropertiesCredentials(credentials), new ClientConfiguration().withMaxErrorRetry(5));
        schema = new SimpleDBSchema(amazonSimpleDBClient);
        return new SimpleDBRecords(amazonSimpleDBClient, true, new Mappings(), logger, schema);
    }

    @Override
    @Ignore("Not Supported by AWS")
    public void supportsAliasingAKeyword() throws Exception {
    }

    @Test
    public void canAddMoreThat25RecordsAtATimeAndReceiveMoreThanAHundred() throws Exception {
        assertThat(records.get(books).size(), is(3));
        Sequence<Record> newBooks = repeat(record().set(isbn, zenIsbn)).take(100);
        assertThat(newBooks.size(), NumberMatcher.is(100));
        records.add(books, newBooks);
        assertThat(records.get(books).size(), is(103));
    }
}
