package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

public interface Queryable<T> {
    Sequence<Record> query(final T query, final Sequence<Keyword<?>> definitions);
}
