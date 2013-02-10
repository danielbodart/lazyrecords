package com.googlecode.lazyrecords.sql.expressions;

public class AnsiWhereClause extends CompoundExpression implements WhereClause {
    private final Expression expression;

    public AnsiWhereClause(Expression expression) {
        super(prefixWhere(expression));
        this.expression = expression;
    }

    private static Expression prefixWhere(Expression expression) {
        if (Expressions.isEmpty(expression)) return expression;
        return where.join(expression);
    }

    public static WhereClause whereClause(Expression expression) {
        return new AnsiWhereClause(expression);
    }

    @Override
    public Expression expression() {
        return expression;
    }
}