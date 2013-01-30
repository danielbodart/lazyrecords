package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;

public interface SelectList extends Expression {
    Sequence<DerivedColumn> derivedColumns();
}
