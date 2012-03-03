package com.googlecode.lazyrecords.xml.mappings;

import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Sequence;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static com.googlecode.totallylazy.Xml.contents;

public class ObjectMapping implements XmlMapping<Object> {
    private final StringMapping stringMapping = new StringMapping();
    private final Class<?> aClass;
    private final StringMappings stringMappings;

    public ObjectMapping(Class<?> aClass, StringMappings stringMappings) {
        this.aClass = aClass;
        this.stringMappings = stringMappings;
    }

    public Sequence<Node> to(Document document, String expression, Object value) {
        return stringMapping.to(document, expression, stringMappings.toString(aClass, value));
    }

    public Object from(Sequence<Node> nodes) {
        return stringMappings.toValue(aClass, stringMapping.from(nodes));
    }
}
