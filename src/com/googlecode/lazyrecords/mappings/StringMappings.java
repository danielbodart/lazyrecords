package com.googlecode.lazyrecords.mappings;

import com.googlecode.totallylazy.Unchecked;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.totallylazy.Unchecked.cast;

public class StringMappings {
    private final Map<Class, Mapping<Object>> map = new HashMap<Class, Mapping<Object>>();

    public StringMappings() {
        add(Date.class, new DateMapping());
        add(Integer.class, new IntegerMapping());
        add(Long.class, new LongMapping());
        add(URI.class, new UriMapping());
        add(Boolean.class, new BooleanMapping());
        add(UUID.class, new UUIDMapping());
    }

    public <T> StringMappings add(final Class<T> type, final Mapping<T> mapping) {
        map.put(type, Unchecked.<Mapping<Object>>cast(mapping));
        return this;
    }

    public <T> Mapping<T> get(final Class<? extends T> aClass) {
        if (!map.containsKey(aClass)) {
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