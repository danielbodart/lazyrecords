package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;

public class CsvWriterTest {

    @Test
    public void shouldWorkWhenFieldsAreMissingInThenMiddle() throws Exception {
        Writer writer = new StringWriter();
        Keyword<?> keywordA = Keywords.keyword("A", String.class);
        Keyword<?> keywordB = Keywords.keyword("B", String.class);
        Keyword<?> keywordC = Keywords.keyword("C", String.class);
        Keyword<?> keywordD = Keywords.keyword("D", String.class);

        Sequence<Keyword<?>> keywords = Sequences.sequence(keywordA, keywordB, keywordC, keywordD);

        Pair<Keyword<?>, Object> record1 = Pair.<Keyword<?>, Object>pair(keywordA, "1");
        Pair<Keyword<?>, Object> record2 = Pair.<Keyword<?>, Object>pair(keywordB, "2");

        Sequence<Record> recordSequence = Sequences.sequence(Record.constructors.record(Sequences.one(record1)),Record.constructors.record(Sequences.one(record2)));

        CsvWriter.writeTo(recordSequence,writer,keywords);
        System.out.println("writer.toString() = " + writer.toString());
        String expected ="A,B,C,D\n1,,,\n,2,,\n";
        System.out.println("expected = " + expected);

        assertEquals(expected, writer.toString());
    }

}
