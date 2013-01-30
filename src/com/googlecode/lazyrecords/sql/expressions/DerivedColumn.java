package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

public interface DerivedColumn extends Expression {
    ValueExpression valueExpression();
    Option<AsClause> asClause();
}
