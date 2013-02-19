package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.xml.mappings.XmlMapping;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.Xml;
import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.xml.mappings.XmlMappings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlRecords extends AbstractRecords {
    private final Node root;
    private final XmlMappings mappings;

    public XmlRecords(Node root, XmlMappings mappings) {
        this.root = root instanceof Document ? ((Document) root).getDocumentElement() : root;
        this.mappings = mappings;
    }

    public XmlRecords(Node node) {
        this(node, new XmlMappings());
    }

    public Sequence<Record> get(Definition definition) {
        Sequence<Node> nodes = Xml.selectNodes(root, definition.name());
        return new XmlSequence(nodes, mappings, definition.fields());
    }

    public Number add(Definition definition, Sequence<Record> records) {
        for (Record record : records) {
            Element newElement = record.keywords().fold(root.getOwnerDocument().createElement(toTagName(definition.name())), addNodes(record));
            root.appendChild(newElement);
        }
        return records.size();
    }

    private Function2<Element, Keyword<?>, Element> addNodes(final Record record) {
        return new Function2<Element, Keyword<?>, Element>() {
            public Element call(Element container, Keyword<?> field) throws Exception {
                Object value = record.get(field);
                if (value != null) {
                    XmlMapping<Object> objectMapping = mappings.get(field.forClass());
                    Sequence<Node> nodes = objectMapping.to(root, field.name(), value);
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

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        Sequence<Node> map = get(definition).filter(predicate).<Value<Node>>unsafeCast().map(Callables.<Node>value());
        return Xml.remove(map).size();
    }

    public Number remove(Definition definition) {
        return Xml.remove(root, definition.name()).size();
    }
}
