package com.googlecode.lazyrecords.xml.mappings;

import com.googlecode.lazyrecords.mappings.IntegerMapping;
import com.googlecode.lazyrecords.mappings.LongMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Unchecked;

import java.util.HashMap;
import java.util.Map;

public class XmlMappings {
    private final Map<Class, XmlMapping<Object>> map = new HashMap<Class, XmlMapping<Object>>();
    private final StringMappings stringMappings;

    public XmlMappings(StringMappings stringMappings) {
        this.stringMappings = stringMappings;
        stringMappings.add(Integer.class, new IntegerMapping());
        stringMappings.add(Long.class, new LongMapping());
        add(String.class, new StringMapping());
    }

    public XmlMappings() {
        this(new StringMappings());
    }

    public <T> XmlMappings add(final Class<T> type, final XmlMapping<T> mapping) {
        map.put(type, Unchecked.<XmlMapping<Object>>cast(mapping));
        return this;
    }

    public <T> XmlMappings add(final Class<T> type, final com.googlecode.lazyrecords.mappings.StringMapping<T> mapping) {
        stringMappings.add(type, mapping);
        return this;
    }

    public XmlMapping<Object> get(final Class aClass) {
        if (!map.containsKey(aClass)) {
            return new ObjectMapping(aClass, stringMappings);
        }
        return map.get(aClass);
    }
}
