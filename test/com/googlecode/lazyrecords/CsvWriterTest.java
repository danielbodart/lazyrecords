package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

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
        Keyword<?> keywordA = Keywords.keyword("A", String.class);
        Keyword<?> keywordB = Keywords.keyword("B", String.class);
        Keyword<?> keywordC = Keywords.keyword("C", String.class);
        Keyword<?> keywordD = Keywords.keyword("D", String.class);
        Sequence<Keyword<?>> keywords = sequence(keywordA, keywordB, keywordC, keywordD);

        Pair<Keyword<?>, Object> record1 = Pair.<Keyword<?>, Object>pair(keywordA, "1");
        Pair<Keyword<?>, Object> record2 = Pair.<Keyword<?>, Object>pair(keywordB, "2");
        Sequence<Record> recordSequence = sequence(record(one(record1)), record(one(record2)));


        CsvWriter.writeTo(recordSequence.iterator(), writer, keywords);

        String expected =
                "A,B,C,D\n" +
                        "1,,,\n" +
                        ",2,,\n";
        assertEquals(expected, writer.toString());
    }

    @Test
    public void shouldQuoteFieldsContainingCommas() throws Exception {
        Writer writer = new StringWriter();
        Keyword<?> keywordA = Keywords.keyword("A", String.class);


        Pair<Keyword<?>, Object> record1 = Pair.<Keyword<?>, Object>pair(keywordA, "Well,hello there,sugar");
        Sequence<Record> recordSequence = sequence(record(one(record1)));


        CsvWriter.writeTo(recordSequence.iterator(), writer, Sequences.<Keyword<?>>one(keywordA));

        assertThat(writer.toString(), is("A\n\"Well,hello there,sugar\"\n"));
    }
}
