package com.googlecode.lazyrecords;

public interface Metadata<Self extends Metadata<Self>> {
    Record metadata();

    Self metadata(Record record);

    <T> T metadata(Keyword<T> keyword);

    <T> Self metadata(Keyword<T> keyword, T value);
}
