package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.ColumnReference.columnReference;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class JoinCondition extends CompoundExpression implements JoinSpecification {
    public static final TextOnlyExpression on = textOnly("on");
    public static final TextOnlyExpression predicate = textOnly("=");
    private final ColumnReference left;
    private final ColumnReference right;

    private JoinCondition(final ColumnReference left, ColumnReference right) {
        super(on, left, predicate, right);
        this.left = left;
        this.right = right;
    }

    public static JoinCondition joinCondition(final ColumnReference left, ColumnReference right) {
        return new JoinCondition(left, right);
    }

    public static JoinCondition joinCondition(final String left, String right) {
        return new JoinCondition(columnReference(left), columnReference(right));
    }

    public ColumnReference left() {
        return left;
    }

    public ColumnReference right() {
        return right;
    }
}
