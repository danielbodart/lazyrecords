package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;

public class AnsiTablePrimary extends CompoundExpression implements TablePrimary {
    private final TableName tableName;
    private final Option<AsClause> asClause;

    private AnsiTablePrimary(TableName tableName, Option<AsClause> asClause) {
        super(tableName, Expressions.expression(asClause));
        this.tableName = tableName;
        this.asClause = asClause;
    }

    public static TablePrimary tablePrimary(TableName tableName, Option<AsClause> asClause) {
        return new AnsiTablePrimary(tableName, asClause);
    }

    public static TablePrimary tablePrimary(TableName tableName, AsClause asClause) {
        return new AnsiTablePrimary(tableName, some(asClause));
    }

    public static TablePrimary tablePrimary(TableName tableName) {
        return new AnsiTablePrimary(tableName, none(AsClause.class));
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
