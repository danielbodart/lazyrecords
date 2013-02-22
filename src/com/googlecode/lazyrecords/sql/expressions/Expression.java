package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;

public interface Expression {
    String text();

    Sequence<Object> parameters();
}