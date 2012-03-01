package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Xml;
import org.junit.Test;
import org.w3c.dom.Document;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.totallylazy.Xml.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XmlRecordsTest extends RecordsContract<XmlRecords> {
    private Document document;

    public XmlRecordsTest() {
        people = definition("/data/user", people.fields());
        books = definition("/data/book", books.fields());
    }

    @Override
    protected XmlRecords createRecords() throws Exception {
        document = document("<data/>");
        return new XmlRecords(document);
    }

    @Test
    public void showGeneratedXml() throws Exception {
        System.out.println(Xml.format(document.getDocumentElement()));
    }

    @Test
    public void shouldSupportAliasingbyGetting() throws Exception {
        Records xmlRecords = new XmlRecords(document("<data><user><summary><firstName>Dan</firstName></summary></user></data>"));
        Keyword<String> aliased = keyword("summary/firstName", String.class).as("first");
        Definition definition = definition("/data/user", aliased);
        Sequence<Pair<Keyword<?>,Object>> fields = xmlRecords.get(definition).head().fields();
        assertThat(fields, is(Sequences.sequence(Pair.<Keyword<?>, Object>pair(keyword("first", String.class), "Dan"))));
    }
}
