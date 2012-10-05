package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;

import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Sequences.sequence;

public class SourceRecord<T> implements Record, Value<T> {
    private final T value;
    private final Record record;

    private SourceRecord(T value, Record record) {
        this.value = value;
        this.record = record;
    }

    public static <T> SourceRecord<T> record(T value) {
        return record(value, new ImmutableMapRecord());
    }

    public static <T> SourceRecord<T> record(T value, Record record) {
        return new SourceRecord<T>(value, record);
    }

    public static <T> Record record(final T value, final Pair<Keyword<?>, Object>... fields) {
        return record(value, sequence(fields));
    }

    public static <T> Record record(final T value, final Sequence<Pair<Keyword<?>, Object>> fields) {
        return fields.fold(record(value), functions.updateValues());
    }

    public T value() {
        return value;
    }

    @Override
    public <T> T get(Keyword<T> keyword) {
        return record.get(keyword);
    }

    @Override
    public <T> Option<T> getOption(Keyword<T> keyword) {
        return option(get(keyword));
    }

    @Override
    public <T> Record set(Keyword<T> name, T value) {
        return record(this.value(), record.set(name, value));
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