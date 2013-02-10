package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;

public class AnsiSelectList extends CompoundExpression implements SelectList {
    private final Sequence<DerivedColumn> derivedColumns;

    public AnsiSelectList(Sequence<DerivedColumn> derivedColumns) {
        super(derivedColumns, ", ");
        this.derivedColumns = derivedColumns;
    }

    public static SelectList selectList(Sequence<DerivedColumn> map) {
        return new AnsiSelectList(map);
    }

    @Override
    public Sequence<DerivedColumn> derivedColumns() {
        return derivedColumns;
    }
}
