package com.googlecode.lazyrecords;

public class Schemaless implements Schema {
    @Override
    public void define(Definition definition) {
    }

    @Override
    public boolean exists(Definition definition) {
        return true;
    }

    @Override
    public void undefine(Definition definition) {
    }
}
