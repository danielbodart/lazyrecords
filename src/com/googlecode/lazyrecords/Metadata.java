package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Option;

public interface Metadata<Self extends Metadata<Self>> {
    Record metadata();

    Self metadata(Record record);

    <T> Option<T> metadata(Keyword<T> keyword);

    <T> Self metadata(Keyword<T> keyword, T value);
}
