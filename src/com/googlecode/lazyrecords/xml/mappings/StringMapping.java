package com.googlecode.lazyrecords.xml.mappings;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static com.googlecode.totallylazy.Xml.contents;
import static com.googlecode.lazyrecords.xml.XmlRecords.toTagName;

public class StringMapping implements XmlMapping<String> {
    public Sequence<Node> to(Node node, String expression, String value) {
        Element element = node.getOwnerDocument().createElement(toTagName(expression));
        element.appendChild(node.getOwnerDocument().createTextNode(value));
        return Sequences.<Node>sequence(element);
    }

    public String from(Sequence<Node> nodes) {
        return contents(nodes);
    }
}
