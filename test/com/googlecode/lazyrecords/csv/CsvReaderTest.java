package com.googlecode.lazyrecords.csv;

import com.googlecode.lazyrecords.Keyword;
import org.junit.Test;

import java.io.StringReader;
import java.util.Date;

import static com.googlecode.lazyrecords.csv.CsvReader.Grammar.ESCAPED_QUOTE;
import static com.googlecode.lazyrecords.csv.CsvReader.Grammar.QUOTED;
import static com.googlecode.lazyrecords.csv.CsvReader.constructors.csvReader;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.mappings.StringMappings.functions.fromString;
import static com.googlecode.lazyrecords.mappings.StringMappings.javaMappings;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CsvReaderTest {
    @Test
    public void parsesSingleColumn() throws Exception {
        Keyword<String> a = keyword("A", String.class);
        String sampleCsv = "A\n" + "a";

        assertThat(csvReader.read(new StringReader(sampleCsv)), is(one(record(a, "a"))));
    }

    @Test
    public void parsesSimpleFile() throws Exception {
        Keyword<String> a = keyword("A", String.class);
        Keyword<String> b = keyword("B", String.class);
        Keyword<String> c = keyword("C", String.class);
        String sampleCsv = "A,B,C\n" + "a,b,c";

        assertThat(csvReader.read(new StringReader(sampleCsv)), is(one(record(a, "a", b, "b", c, "c"))));
    }

    @Test
    public void parsesFileWithQuotedValue() throws Exception {
        Keyword<String> a = keyword("A", String.class);
        Keyword<String> b = keyword("B", String.class);
        Keyword<String> c = keyword("C", String.class);
        String sampleCsv = "A,B,C\n" + "a,\"b,b,b\",\"a\nnew line\"";

        assertThat(csvReader.read(new StringReader(sampleCsv)), is(one(record(a, "a", b, "b,b,b", c, "a\nnew line"))));
    }

    @Test
    public void parsesFileWithLotsOfQuotes() throws Exception {
        Keyword<String> a = keyword("A", String.class);
        Keyword<String> b = keyword("B", String.class);
        Keyword<String> c = keyword("C", String.class);
        String sampleCsv = "A,B,C\n" + "\"\",\"\"\"b\"\"\",\"\"\"\"";
        assertThat(csvReader.read(new StringReader(sampleCsv)), is(one(record(a, "", b, "\"b\"", c, "\""))));
    }

    @Test
    public void canMapTypes() throws Exception {
        Keyword<Integer> a = keyword("A", Integer.class);
        Keyword<Long> b = keyword("B", Long.class);
        Keyword<Date> c = keyword("C", Date.class);
        Date someDate = date(2001, 1, 1);
        String sampleCsv = "A,B,C\n" + "1,2," + someDate;

        assertThat(csvReader.read(new StringReader(sampleCsv)).map(fromString(javaMappings(), sequence(a, b, c))),
                is(one(record(a, 1, b, 2L, c, someDate))));
    }

    @Test
    public void parsesEscapedQuotes() throws Exception {
        assertThat(ESCAPED_QUOTE.parse("\"\""), is("\""));
    }

    @Test
    public void parsesEmptyQuote() throws Exception {
        assertThat(QUOTED.parse("\"\""), is(""));
    }

    @Test
    public void parsesSingleQuotedQuote() throws Exception {
        assertThat(QUOTED.parse("\"\"\"\""), is("\""));
    }

    @Test
    public void parsesQuotedValue() throws Exception {
        assertThat(QUOTED.parse("\"\"\"b\"\"\""), is("\"b\""));
    }
}
