package com.googlecode.lazyrecords.mappings;

public interface StringMapping<T> {
    T toValue(String value) throws Exception;

    String toString(T value) throws Exception;
}
