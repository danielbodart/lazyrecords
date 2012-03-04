package com.googlecode.lazyrecords;

import java.util.Map;

public class IgnoreLogger implements Logger {
    @Override
    public Logger log(Map<String, ?> parameters) {
        return this;
    }
}
