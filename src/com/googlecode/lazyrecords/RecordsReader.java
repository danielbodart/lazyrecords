package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;

public interface RecordsReader {
    Sequence<Record> get(Definition definition);
}
