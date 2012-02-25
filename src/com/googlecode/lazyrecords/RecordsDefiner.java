package com.googlecode.lazyrecords;

public interface RecordsDefiner {
    void define(Definition definition);

    boolean exists(Definition definition);

    void undefine(Definition definition);
}
