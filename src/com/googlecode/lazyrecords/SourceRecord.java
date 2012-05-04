package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;

import static com.googlecode.totallylazy.Sequences.sequence;

public class SourceRecord<T> extends MapRecord implements Value<T> {
    private final T value;

    public SourceRecord(T value) {
        this.value = value;
    }

    public static <T> SourceRecord<T> record(T value) {
        return new SourceRecord<T>(value);
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
}
