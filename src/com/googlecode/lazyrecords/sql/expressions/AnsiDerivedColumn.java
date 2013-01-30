package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

public class AnsiDerivedColumn extends CompoundExpression implements DerivedColumn {
    private final ValueExpression valueExpression;
    private final Option<AsClause> asClause;

    private AnsiDerivedColumn(ValueExpression valueExpression, Option<AsClause> asClause) {
        super(valueExpression, Expressions.expression(asClause));
        this.valueExpression = valueExpression;
        this.asClause = asClause;
    }

    public static AnsiDerivedColumn derivedColumn(ValueExpression valueExpression, Option<AsClause> asClause) {
        return new AnsiDerivedColumn(valueExpression, asClause);
    }

    @Override
    public ValueExpression valueExpression() {
        return valueExpression;
    }

    @Override
    public Option<AsClause> asClause() {
        return asClause;
    }
}
