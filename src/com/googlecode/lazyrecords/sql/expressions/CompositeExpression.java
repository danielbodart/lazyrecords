package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;

public class CompositeExpression extends CompoundExpression implements ValueExpression {
    private final Sequence<ColumnReference> columnReferences;

    public CompositeExpression(Sequence<ColumnReference> columnReferences, String start, String separator, String end) {
        super(columnReferences, start, separator, end);
        this.columnReferences = columnReferences;
    }

    public Sequence<ColumnReference> columnReferences() {
        return columnReferences;
    }
}
