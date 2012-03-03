package com.googlecode.lazyrecords.mappings;

public class EnumMapping<E extends Enum<E>> implements StringMapping<E> {
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
