package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordsReader;
import com.googlecode.lazyrecords.xml.mappings.XmlMappings;
import com.googlecode.totallylazy.Sequence;
import org.w3c.dom.Document;

import static com.googlecode.totallylazy.Xml.selectNodes;
import static com.googlecode.totallylazy.Xml.selectNodesForwardOnly;

public class ForwardOnlyXmlRecordsReader implements RecordsReader {
    private final XmlMappings mappings;
    private final Document document;

    public ForwardOnlyXmlRecordsReader(Document document) {
        this.document = document;
        this.mappings = new XmlMappings();
    }

    public Sequence<Record> get(Definition definition) {
        return new XmlSequence(selectNodesForwardOnly(document, definition.name()), mappings, definition.fields());
    }
}
