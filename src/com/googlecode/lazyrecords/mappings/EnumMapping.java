package com.googlecode.lazyrecords.mappings;

import java.util.Formatter;

public class EnumMapping<E extends Enum<E>> implements LexicalMapping<E>{
    private final Class<E> aClass;

    public EnumMapping(Class<E> aClass) {
        this.aClass = aClass;
    }

    @Override
    public E toValue(String value) throws Exception {
        return Enum.valueOf(aClass, value);
    }

    @Override
    public String toString(E value) throws Exception {
        return value.name();
    }
}
