package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

public class DelegatingRecord implements Record {
    private Record record;

    public DelegatingRecord(Record record) {
        this.record = record;
    }

    @Override
    public <T> T get(Keyword<T> keyword) {
        return record.get(keyword);
    }

    @Override
    public <T> Option<T> getOption(Keyword<T> keyword) {
        return record.getOption(keyword);
    }

    @Override
    public <T> Record set(Keyword<T> name, T value) {
        return record.set(name, value);
    }

    @Override
    public Sequence<Pair<Keyword<?>, Object>> fields() {
        return record.fields();
    }

    @Override
    public Sequence<Keyword<?>> keywords() {
        return record.keywords();
    }

    @Override
    public Sequence<Object> getValuesFor(Sequence<Keyword<?>> keywords) {
        return record.getValuesFor(keywords);
    }

    @Override
    public int hashCode() {
        return record.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return record.equals(obj);
    }

    @Override
    public String toString() {
        return record.toString();
    }
}
