package com.googlecode.lazyrecords.mappings;

import java.lang.reflect.InvocationTargetException;

import static com.googlecode.totallylazy.Unchecked.cast;

public class ObjectMapping<T> implements LexicalMapping<T> {
    private final Class<? extends T> aClass;

    public ObjectMapping(Class<? extends T> aClass) {
        this.aClass = aClass;
    }

    public T toValue(String value) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(aClass.equals(Object.class)){
            return cast(value);
        }
        return aClass.getConstructor(String.class).newInstance(value);
    }

    public String toString(T value) {
        return value.toString();
    }
}
