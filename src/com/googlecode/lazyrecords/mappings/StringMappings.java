package com.googlecode.lazyrecords.mappings;

import com.googlecode.totallylazy.Unchecked;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.totallylazy.Unchecked.cast;

public class StringMappings {
    private final Map<Class, StringMapping<Object>> map = new HashMap<Class, StringMapping<Object>>();

    public StringMappings() {
        add(Date.class, new DateMapping());
        add(Integer.class, new LexicalIntegerMapping());
        add(Long.class, new LexicalLongMapping());
        add(URI.class, new UriMapping());
        add(Boolean.class, new BooleanMapping());
        add(UUID.class, new UUIDMapping());
    }

    public <T> StringMappings add(final Class<T> type, final StringMapping<T> mapping) {
        map.put(type, Unchecked.<StringMapping<Object>>cast(mapping));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> StringMapping<T> get(final Class<? extends T> aClass) {
        if (!map.containsKey(aClass)) {
            if(aClass.isEnum()){
                return new EnumMapping(aClass);
            }
            return new ObjectMapping<T>(aClass);
        }
        return cast(map.get(aClass));
    }

    public <T> String toString(final Class<? extends T> aClass, T value) {
        try {
            return value == null ? null : get(aClass).toString(value);
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
    }

    public <T> T toValue(final Class<T> aClass, String value) {
        try {
            return value == null ? null : aClass.cast(get(aClass).toValue(value));
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
    }



}