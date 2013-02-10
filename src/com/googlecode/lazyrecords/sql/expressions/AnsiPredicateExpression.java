package com.googlecode.lazyrecords.sql.expressions;

public class AnsiPredicateExpression extends CompoundExpression implements PredicateExpression {
    private final ValueExpression predicand;
    private final Expression predicate;

    private AnsiPredicateExpression(ValueExpression predicand, Expression predicate) {
        super(predicand, predicate);
        this.predicand = predicand;
        this.predicate = predicate;
    }

    public static AnsiPredicateExpression predicateExpression(ValueExpression predicand, Expression predicate) {
        return new AnsiPredicateExpression(predicand, predicate);
    }

    @Override
    public ValueExpression predicand() {
        return predicand;
    }

    @Override
    public Expression predicate() {
        return predicate;
    }
}
