package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.collections.PersistentMap;
import com.googlecode.totallylazy.collections.ListMap;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;

public class PersistentRecord implements Record {
    private final PersistentMap<Keyword<?>, Object> fields;

    // See Record.constructors.record to create
    PersistentRecord(PersistentMap<Keyword<?>, Object> fields) {
        this.fields = fields;
    }

    PersistentRecord() {
        this(ListMap.<Keyword<?>, Object>emptyListMap());
    }

    public <T> T get(Keyword<T> keyword) {
        Object value = fields.lookup(keyword).getOrNull();
        Class<T> aClass = keyword.forClass();
        return aClass.cast(value);
    }

    @Override
    public <T> Option<T> getOption(Keyword<T> keyword) {
        return option(get(keyword));
    }

    public <T> Record set(Keyword<T> name, T value) {
        return new PersistentRecord(fields.insert(name, value));
    }

    public Sequence<Pair<Keyword<?>, Object>> fields() {
        return fields.toSequence();
    }

    public Sequence<Keyword<?>> keywords() {
        return fields().map(Callables.<Keyword<?>>first());
    }

    public <T> Sequence<T> valuesFor(Sequence<? extends Keyword<? extends T>> keywords) {
        return fields().
                filter(where(Callables.<Keyword<?>>first(), Predicates.<Keyword<?>>in(keywords))).
                map(second()).
                unsafeCast();
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
