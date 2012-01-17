package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Xml;
import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.xml.mappings.Mapping;
import com.googlecode.lazyrecords.xml.mappings.Mappings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;

import static com.googlecode.totallylazy.Xml.xpath;

public class XmlRecords extends AbstractRecords {
    private final XPath xpath = xpath();
    private final Document document;
    private final Mappings mappings;

    public XmlRecords(Document document, Mappings mappings) {
        this.document = document;
        this.mappings = mappings;
    }

    public XmlRecords(Document document) {
        this(document, new Mappings());
    }

    public Sequence<Record> get(RecordName recordName) {
        Sequence<Node> nodes = Xml.selectNodes(document, recordName.value());
        return new XmlSequence(nodes, mappings, definitions(recordName));
    }

    public Number add(RecordName recordName, Sequence<Record> records) {
        for (Record record : records) {
            Element newElement = record.keywords().fold(document.createElement(toTagName(recordName.value())), addNodes(record));
            Node parent = Xml.selectNodes(document, toParent(recordName)).head();
            parent.appendChild(newElement);
        }
        return records.size();
    }

    private String toParent(RecordName recordName) {
        String xpath = recordName.value();
        String[] parts = xpath.split("/");
        return Sequences.sequence(parts).take(parts.length - 1).toString("/");
    }

    @SuppressWarnings("unchecked")
    private Function2<? super Element, ? super Keyword, Element> addNodes(final Record record) {
        return new Function2<Element, Keyword, Element>() {
            public Element call(Element container, Keyword field) throws Exception {
                Object value = record.get(field);
                if (value != null) {
                    Mapping<Object> objectMapping = mappings.get(field.forClass());
                    Sequence<Node> nodes = objectMapping.to(document, field.toString(), value);
                    for (Node node : nodes) {
                        container.appendChild(node);
                    }
                }
                return container;
            }
        };
    }

    public static String toTagName(final String expression) {
        String[] parts = expression.split("/");
        return parts[parts.length - 1];
    }

    public Number remove(RecordName recordName, Predicate<? super Record> predicate) {
        Sequence<Node> map = get(recordName).filter(predicate).map(asNode());
        return Xml.remove(map).size();
    }

    @SuppressWarnings("unchecked")
    private Function1<? super Record, Node> asNode() {
        return new Function1<Record, Node>() {
            public Node call(Record record) throws Exception {
                return ((SourceRecord<Node>) record).value();
            }
        };
    }

    public Number remove(RecordName recordName) {
        return Xml.remove(document, recordName.value()).size();
    }
}
