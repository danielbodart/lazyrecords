package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.RecordDefinition;
import com.googlecode.totallylazy.Xml;
import com.googlecode.lazyrecords.RecordsContract;
import org.junit.Test;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.document;

public class XmlRecordsTest extends RecordsContract<XmlRecords> {
    private Document document;

    public XmlRecordsTest() {
        people = RecordDefinition.definition("/data/user", people.fields());
        books = RecordDefinition.definition("/data/book", books.fields());
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
