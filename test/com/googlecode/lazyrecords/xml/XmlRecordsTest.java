package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.Xml;
import com.googlecode.lazyrecords.AbstractRecordsTests;
import com.googlecode.lazyrecords.Keywords;
import org.junit.Test;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.document;

public class XmlRecordsTest extends AbstractRecordsTests<XmlRecords>{
    private Document document;

    public XmlRecordsTest() {
        people = RecordName.recordName("/data/user");
        books = RecordName.recordName("/data/book");
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
}
