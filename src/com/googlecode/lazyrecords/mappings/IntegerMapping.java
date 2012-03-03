package com.googlecode.lazyrecords.mappings;

import com.googlecode.totallylazy.numbers.Numbers;

public class IntegerMapping implements LexicalMapping<Integer> {
    public Integer toValue(String value) {
        return Numbers.parseLexicalString(value, Integer.MIN_VALUE).intValue();
    }

    public String toString(Integer value) {
        return Numbers.toLexicalString(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
