package com.googlecode.lazyrecords.mappings;

public class IntegerMapping implements StringMapping<Integer> {
    public Integer toValue(String value) {
        return Integer.parseInt(value);
    }

    public String toString(Integer value) {
        return Integer.toString(value);
    }
}
