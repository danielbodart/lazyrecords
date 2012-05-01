package com.googlecode.lazyrecords.xml.mappings;

import com.googlecode.totallylazy.Sequence;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface XmlMapping<T> {
    Sequence<Node> to(Document document, String expression, T value) throws Exception;

    T from(Sequence<Node> nodes) throws Exception;


}
