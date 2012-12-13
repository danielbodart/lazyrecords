package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.collections.PersistentMap;
import com.googlecode.totallylazy.collections.ListMap;

import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class ImmutableMapRecord implements Record {
    private final PersistentMap<Keyword<?>, Object> fields;

    // See Record.constructors.PersistentRecord to create
    ImmutableMapRecord(PersistentMap<Keyword<?>, Object> fields) {
        this.fields = fields;
    }

    ImmutableMapRecord() {
        this(ListMap.<Keyword<?>, Object>emptyListMap());
    }

    public <T> T get(Keyword<T> keyword) {
        Object value = fields.get(keyword).getOrNull();
        Class<T> aClass = keyword.forClass();
        return aClass.cast(value);
    }

    @Override
    public <T> Option<T> getOption(Keyword<T> keyword) {
        return option(get(keyword));
    }

    public <T> Record set(Keyword<T> name, T value) {
        return new ImmutableMapRecord(fields.put(name, value));
    }

    public Sequence<Pair<Keyword<?>, Object>> fields() {
        return fields.persistentList().toSequence();
    }

    public Sequence<Keyword<?>> keywords() {
        return fields().map(Callables.<Keyword<?>>first());
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
