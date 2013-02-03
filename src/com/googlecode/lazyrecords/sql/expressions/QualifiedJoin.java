package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface QualifiedJoin extends TableReference {
    TableReference left();
    JoinType joinType();
    TextOnlyExpression join = textOnly("join");
    TableReference right();
    JoinSpecification joinSpecification();
}
