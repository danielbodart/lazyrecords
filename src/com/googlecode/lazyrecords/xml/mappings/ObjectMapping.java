package com.googlecode.lazyrecords.xml.mappings;

import com.googlecode.lazyrecords.mappings.LexicalMappings;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static com.googlecode.lazyrecords.xml.XmlRecords.toTagName;
import static com.googlecode.totallylazy.Xml.contents;

public class ObjectMapping implements XmlMapping<Object> {
    private final StringMapping stringMapping = new StringMapping();
    private final Class<?> aClass;
    private final LexicalMappings lexicalMappings;

    public ObjectMapping(Class<?> aClass, LexicalMappings lexicalMappings) {
        this.aClass = aClass;
        this.lexicalMappings = lexicalMappings;
    }

    public Sequence<Node> to(Document document, String expression, Object value) {
        return stringMapping.to(document, expression, lexicalMappings.toString(aClass, value));
    }

    public Object from(Sequence<Node> nodes) {
        return lexicalMappings.toValue(aClass, stringMapping.from(nodes));
    }
}
