package com.googlecode.lazyrecords;

public interface RecordsDefiner {
    void define(Definition definition);

    boolean exists(Definition definition);

    boolean undefine(Definition definition);
}
