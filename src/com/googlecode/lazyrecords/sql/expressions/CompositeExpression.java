package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;

public class CompositeExpression extends CompoundExpression implements ValueExpression {
    public CompositeExpression(Sequence<? extends Expression> expressions, String start, String separator, String end) {
        super(expressions, start, separator, end);
    }
}
