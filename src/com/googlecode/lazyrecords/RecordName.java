package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Value;

public class RecordName implements Value<String> {
    private final String value;

    private RecordName(String value) {
        this.value = value;
    }

    public static RecordName recordName(String value) {
        return new RecordName(value);
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
