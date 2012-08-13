package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.junit.Assert.assertEquals;

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

        String expected =
                "A, B, C, D\n" +
                "1, , , \n" +
                ", 2, , \n";

        CsvWriter.writeTo(recordSequence.iterator(), writer, keywords);

        assertEquals(expected, writer.toString());
    }
}
