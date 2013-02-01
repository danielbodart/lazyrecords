package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.lazyrecords.Record;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.columnReference;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.AnsiWhereClause.whereClause;

public class AnsiUpdateStatement extends CompoundExpression implements UpdateStatement {
    private final TableName tableName;
    private final SetClause setClause;
    private final WhereClause whereClause;

    private AnsiUpdateStatement(TableName tableName, SetClause setClause, WhereClause whereClause) {
        super(update.join(tableName), setClause, whereClause);
        this.tableName = tableName;
        this.setClause = setClause;
        this.whereClause = whereClause;
    }

    public static UpdateStatement updateStatement(TableName tableName, SetClause setClause, WhereClause whereClause) {
        return new AnsiUpdateStatement(tableName, setClause, whereClause);
    }

    @Override
    public TableName tableName() {
        return tableName;
    }

    @Override
    public SetClause setClause() {
        return setClause;
    }

    @Override
    public WhereClause whereClause() {
        return whereClause;
    }
}
