package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Sequence;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import java.lang.ref.WeakReference;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.totallylazy.Xml.document;
import static com.googlecode.totallylazy.matchers.NumberMatcher.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

public class ForwardOnlyXmlRecordsReaderTest {
    @Test
    @Ignore
    public void doesNotStopDocumentFromBeingGarbageCollectedOnceConsumed() throws Exception {
        WeakReference<Document> document = new WeakReference<Document>(document("<data><child name='dan'/><child name='tom'/></data>"));

        Keyword<String> name = keyword("@name", String.class);
        Sequence<Record> results = new ForwardOnlyXmlRecordsReader(document.get()).get(Definition.constructors.definition("//child", name));
        assertThat(results.size(), is(2));

        System.gc();

        assertThat(document.get(), Matchers.is(nullValue()));
        assertThat(results.size(), is(0));
    }
}
