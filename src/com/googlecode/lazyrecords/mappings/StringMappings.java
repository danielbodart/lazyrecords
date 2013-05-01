package com.googlecode.lazyrecords.mappings;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.Uri;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class StringMappings {
    private final Map<Class, StringMapping<Object>> map = map();

    public StringMappings() {
        add(Date.class, new DateMapping());
        add(Integer.class, new LexicalIntegerMapping());
        add(Long.class, new LexicalLongMapping());
        add(URI.class, new JavaURIMapping());
        add(Uri.class, new UriMapping());
        add(Boolean.class, new BooleanMapping());
        add(UUID.class, new UUIDMapping());
    }

    public static StringMappings javaMappings() {
        return lexicalMappings().
                add(Integer.class, new IntegerMapping()).
                add(Long.class, new LongMapping());
    }

    public static StringMappings lexicalMappings() {
        return new StringMappings();
    }

    public <T> StringMappings add(final Class<T> type, final StringMapping<T> mapping) {
        map.put(type, Unchecked.<StringMapping<Object>>cast(mapping));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> StringMapping<T> get(final Class<? extends T> aClass) {
        if (!map.containsKey(aClass)) {
            if (aClass.isEnum()) {
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

    public static class functions {
        public static UnaryFunction<Record> fromString(final StringMappings mappings, final Iterable<? extends Keyword<?>> keywords) {
            return new UnaryFunction<Record>() {
                @Override
                public Record call(Record record) throws Exception {
                    return sequence(keywords).fold(record, new Function2<Record, Keyword<?>, Record>() {
                        @Override
                        public Record call(Record record, Keyword<?> keyword) throws Exception {
                            String raw = record.get(keyword(keyword.name(), String.class));
                            Object value = mappings.toValue(keyword.forClass(), raw);
                            return record.set(keyword(keyword.name()), value);
                        }
                    });
                }
            };
        }

    }


}