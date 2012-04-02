package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Maps.pairs;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.sequence;

public class MapRecord implements Record {
    private final Map<Keyword<?>, Object> fields = new LinkedHashMap<Keyword<?>, Object>();

    // See Record.constructors.record to create
    MapRecord() {
    }

    public <T> T get(Keyword<T> keyword) {
        Object value = fields.get(keyword);
        Class<T> aClass = keyword.forClass();
        return aClass.cast(value);
    }

    public <T> Record set(Keyword<T> name, T value) {
        fields.put(name, value);
        return this;
    }

    public Sequence<Pair<Keyword<?>, Object>> fields() {
        return pairs(fields);
    }

    public Sequence<Keyword<?>> keywords() {
        return sequence(fields.keySet());
    }

    public Sequence<Object> getValuesFor(Sequence<Keyword<?>> keywords) {
        return fields().filter(where(Callables.<Keyword<?>>first(), is(in(keywords)))).map(second());
    }

    @Override
    public String toString() {
        return fields().toString();
    }

    @Override
    public final boolean equals(final Object o) {
        return o instanceof Record && fields().equals(((Record) o).fields());
    }

    @Override
    public final int hashCode() {
        return fields().hashCode();
    }
}
