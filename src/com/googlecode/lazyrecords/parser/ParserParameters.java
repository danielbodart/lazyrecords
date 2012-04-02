package com.googlecode.lazyrecords.parser;

import java.util.Map;

import static com.googlecode.totallylazy.Maps.map;

public class ParserParameters {
    private final Map<String, Object> values = map();

    public ParserParameters add(String name, Object value) {
        values.put(name, value);
        return this;
    }

    public Map<String, Object> values() {
        return values;
    }
}
