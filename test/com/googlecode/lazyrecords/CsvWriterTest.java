package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CsvWriterTest {
    @Test
    public void shouldWorkWhenFieldsAreMissingInThenMiddle() throws Exception {
        Writer writer = new StringWriter();
        Keyword<String> keywordA = keyword("A", String.class);
        Keyword<String> keywordB = keyword("B", String.class);
        Sequence<Keyword<String>> keywords = sequence(keywordA, keywordB, keyword("C", String.class), keyword("D", String.class));

        Record record1 = record(keywordA, "1");
        Record record2 = record(keywordB, "2");
        Sequence<Record> recordSequence = sequence(record1, record2);

        CsvWriter.writeTo(recordSequence.iterator(), writer, keywords);

        String expected = "A,B,C,D\n" +
                "1,,,\n" +
                ",2,,\n";
        assertEquals(expected, writer.toString());
    }

    @Test
    public void shouldQuoteFieldsContainingCommas() throws Exception {
        Writer writer = new StringWriter();
        Keyword<String> keywordA = keyword("A", String.class);

        Record record = record(keywordA, "Well,hello there,sugar");

        CsvWriter.writeTo(one(record).iterator(), writer, one(keywordA));

        assertThat(writer.toString(), is("A\n\"Well,hello there,sugar\"\n"));
    }
}
