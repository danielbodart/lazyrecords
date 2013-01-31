package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

public class AnsiTableReference extends CompoundExpression implements TableReference {
    private final TableName tableName;
    private final Option<AsClause> asClause;

    private AnsiTableReference(TableName tableName, Option<AsClause> asClause) {
        super(tableName, Expressions.expression(asClause));
        this.tableName = tableName;
        this.asClause = asClause;
    }

    public static TableReference tableReference(TableName tableName, Option<AsClause> asClause) {
        return new AnsiTableReference(tableName, asClause);
    }

    @Override
    public TableName tableName() {
        return tableName;
    }

    @Override
    public Option<AsClause> asClause() {
        return asClause;
    }
}
