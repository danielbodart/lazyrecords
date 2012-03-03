package com.googlecode.lazyrecords.mappings;

public class BooleanMapping implements StringMapping<Boolean> {
    public Boolean toValue(String value) {
        return Boolean.parseBoolean(value);
    }

    public String toString(Boolean value) {
        return Boolean.toString(value);
    }
}
