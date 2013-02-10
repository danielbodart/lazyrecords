package com.googlecode.lazyrecords.sql.expressions;

public class AnsiQualifiedJoin extends CompoundExpression implements QualifiedJoin {
    private final TableReference left;
    private final JoinType joinType;
    private final TableReference right;
    private final JoinSpecification joinSpecification;

    private AnsiQualifiedJoin(final TableReference left, final JoinType joinType, final TableReference right, final JoinSpecification joinSpecification) {
        super(left, joinType, join, right, joinSpecification);
        this.left = left;
        this.joinType = joinType;
        this.right = right;
        this.joinSpecification = joinSpecification;
    }

    public static AnsiQualifiedJoin qualifiedJoin(final TableReference left, final JoinType joinType, final TableReference right, final JoinSpecification joinSpecification) {
        return new AnsiQualifiedJoin(left, joinType, right, joinSpecification);
    }

    @Override
    public TableReference left() {
        return left;
    }

    @Override
    public JoinType joinType() {
        return joinType;
    }

    @Override
    public TableReference right() {
        return right;
    }

    @Override
    public JoinSpecification joinSpecification() {
        return joinSpecification;
    }
}
