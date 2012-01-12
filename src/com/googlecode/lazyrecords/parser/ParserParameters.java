package com.googlecode.lazyrecords.parser;

import java.util.HashMap;
import java.util.Map;

public class ParserParameters {
    private final Map<String, Object> values = new HashMap<String, Object>();

    public ParserParameters add(String name, Object value) {
        values.put(name, value);
        return this;
    }

    public Map<String, Object> values() {
        return values;
    }
}
