package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Xml;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Test;
import org.w3c.dom.Document;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.totallylazy.Xml.sequence;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
    public void shouldSupportAliasingByGetting() throws Exception {
        Records xmlRecords = new XmlRecords(document("<data><user><summary><firstName>Dan</firstName></summary></user></data>"));

        Keyword<String> aliased = keyword("summary/firstName", String.class).as("first").setMetadata(Keywords.INDEXED, true);
        Definition definition = definition("/data/user", aliased);
        Sequence<Pair<Keyword<?>,Object>> fields = xmlRecords.get(definition).head().fields();
        assertThat(fields.size(), NumberMatcher.is(1));
        Pair<Keyword<?>, Object> pair = fields.head();
        Keyword<?> keyword = pair.first();
        assertThat(keyword.name(), is("first"));
        assertThat(keyword.forClass().equals(String.class), is(true));
        assertThat(keyword, is(not(instanceOf(AliasedKeyword.class))));
        assertThat(keyword.metadata().fields().size(), NumberMatcher.is(1));
        assertThat(keyword.metadata().get(Keywords.INDEXED), is(true));
    }
}
