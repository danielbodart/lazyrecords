package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.*;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class FromClause extends CompoundExpression {
    public FromClause(Definition table) {
        super(textOnly("from"), name(table));
    }

    public static Expression toSql(Pair<Number, Join> pair) {
        Number index = pair.first();
        Join join = pair.second();
        SelectBuilder select = SelectBuilder.selectBuilder(join);
        String tableAlias = SelectExpression.tableAlias(index);
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

    public static Expression fromClause(Definition definition) {
        return new FromClause(definition);
    }
}
