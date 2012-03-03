package com.googlecode.lazyrecords.xml.mappings;

import com.googlecode.lazyrecords.mappings.LexicalMappings;
import com.googlecode.totallylazy.Unchecked;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Mappings {
    private final Map<Class, XmlMapping<Object>> map = new HashMap<Class, XmlMapping<Object>>();
    private final LexicalMappings lexicalMappings;

    public Mappings(LexicalMappings lexicalMappings) {
        this.lexicalMappings = lexicalMappings;
        add(Date.class, DateMapping.defaultFormat());
        add(Integer.class, new IntegerMapping());
        add(Long.class, new LongMapping());
        add(String.class, new StringMapping());
    }

    public Mappings() {
        this(new LexicalMappings());
    }

    public <T> Mappings add(final Class<T> type, final XmlMapping<T> mapping) {
        map.put(type, Unchecked.<XmlMapping<Object>>cast(mapping));
        return this;
    }

    public XmlMapping<Object> get(final Class aClass) {
        if(!map.containsKey(aClass)) {
            return new ObjectMapping(aClass, lexicalMappings);
        }
        return map.get(aClass);
    }
}
