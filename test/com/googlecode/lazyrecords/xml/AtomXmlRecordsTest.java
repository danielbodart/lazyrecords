package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.xml.mappings.DateMapping;
import com.googlecode.lazyrecords.xml.mappings.XmlMappings;
import org.junit.Test;

import java.net.URI;
import java.util.Date;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.totallylazy.URLs.uri;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AtomXmlRecordsTest {
    private static final Keyword<Integer> id = Keywords.keyword("id", Integer.class);
    private static final Keyword<URI> link = Keywords.keyword("link/@href", URI.class);
    private static final Keyword<String> content = Keywords.keyword("content", String.class);
    private static final Keyword<Date> updated = Keywords.keyword("updated", Date.class);
    private static final Definition entries = definition("/feed/entry", id, link, content, updated);

    @Test
    public void canGetElements() throws Exception {
        Records records = new XmlRecords(document(XML), new XmlMappings().add(Date.class, DateMapping.atomDateFormat()));
        Record record = records.get(entries).head();
        assertThat(record.get(id), is(ID));
        assertThat(record.get(link), is(LINK));
        assertThat(record.get(content), is(CONTENT));
        assertThat(record.get(updated), is(date(2011, 7, 19, 12, 43, 26)));
    }

    public static final Integer ID = 1234;

    private static final String CONTENT = "<event>" +
            "<source>blah</source>" +
            "<payload>foo</payload>" +
            "</event>";

    private static final URI LINK = uri("http://localhost:10010/somePath");

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<feed>" +
            "  <title>Some feed</title>" +
            "  <entry>" +
            "    <title>Some entry</title>" +
            "    <link href=\"" + LINK + "\" />" +
            "    <id>" + ID + "</id>" +
            "<content type=\"text/xml\">" +
            CONTENT +
            "</content>" +
            "    <updated>2011-07-19T12:43:26Z</updated>" +
            "    <summary type=\"text\">Summary</summary>" +
            "  </entry>" +
            "</feed>";
}
