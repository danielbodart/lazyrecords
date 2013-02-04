package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.tableName;

public class AnsiFromClause extends CompoundExpression implements FromClause {
    private final TableReference tableReference;

    private AnsiFromClause(TableReference tableReference) {
        super(from, tableReference);
        this.tableReference = tableReference;
    }

    public static FromClause fromClause(Definition definition) {
        return fromClause(AnsiTablePrimary.tablePrimary(tableName(definition), AnsiAsClause.asClause(definition)));
    }

    public static FromClause fromClause(TableReference tableReference) {
        return new AnsiFromClause(tableReference);
    }

    @Override
    public TableReference tableReference() {
        return tableReference;
    }
}
