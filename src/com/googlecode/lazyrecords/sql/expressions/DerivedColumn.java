package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.one;

public interface DerivedColumn extends Expression {
    ValueExpression valueExpression();

    Option<AsClause> asClause();

    Class<?> forClass();

    class methods{
        public static Sequence<ColumnReference> columnReferences(final DerivedColumn column) {
            ValueExpression value = column.valueExpression();
            if(value instanceof ColumnReference) return one((ColumnReference) value);
            if(value instanceof SetFunctionType) return one(((SetFunctionType) value).columnReference());
            if(value instanceof CompositeExpression) return ((CompositeExpression) value).columnReferences();
            throw new UnsupportedOperationException();
        }
    }
}
