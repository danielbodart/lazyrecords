package com.googlecode.lazyrecords.mappings;

public class ObjectMapping implements Mapping<Object>{
    public Object toValue(String value) {
        return value;
    }

    public String toString(Object value) {
        return value.toString();
    }
}
