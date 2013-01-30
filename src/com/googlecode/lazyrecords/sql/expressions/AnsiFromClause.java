package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class AnsiFromClause extends CompoundExpression implements FromClause {
    private final TableName tableName;

    private AnsiFromClause(TableName tableName) {
        super(from, tableName);
        this.tableName = tableName;
    }

    public static Expression toSql(Pair<Number, Join> pair) {
        Number index = pair.first();
        Join join = pair.second();
        SelectBuilder select = SelectBuilder.selectBuilder(join);
        String tableAlias = AnsiSelectExpression.tableAlias(index);
        Definition definition = select.table().metadata(Keywords.alias, tableAlias);
        Callable1<Record, Predicate<Record>> predicateCreator = join.using();
        if (predicateCreator instanceof Using) {
            Using using = (Using) predicateCreator;
            return textOnly("%s %s using %s", joinExpression(join), name(definition), names(using.keywords()));
        }
        if (predicateCreator instanceof On) {
            On on = (On) predicateCreator;
            return textOnly("%s %s on %s = %s.%s", joinExpression(join), name(definition), name(on.left()), tableAlias, name(on.right()));
        }
        throw new UnsupportedOperationException(format("Unknown expression %s", predicateCreator));
    }

    private static String joinExpression(Join join) {
        if (join instanceof InnerJoin)
            return "inner join";
        if (join instanceof LeftJoin)
            return "left join";
        throw new UnsupportedOperationException(format("Cannot created join expression for %s", join));
    }

    public static FromClause fromClause(Definition definition) {
        return new AnsiFromClause(name(definition));
    }

    @Override
    public TableName tableName() {
        return tableName;
    }
}
