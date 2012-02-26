package com.googlecode.lazyrecords.mappings;

import java.lang.reflect.InvocationTargetException;

public class ObjectMapping<T> implements Mapping<T> {
    private final Class<? extends T> aClass;

    public ObjectMapping(Class<? extends T> aClass) {
        this.aClass = aClass;
    }

    public T toValue(String value) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return aClass.getConstructor(String.class).newInstance(value);
    }

    public String toString(T value) {
        return value.toString();
    }
}
