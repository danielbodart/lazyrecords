package com.googlecode.lazyrecords;

public interface Schema {
    void define(Definition definition);

    boolean exists(Definition definition);

    void undefine(Definition definition);
}
