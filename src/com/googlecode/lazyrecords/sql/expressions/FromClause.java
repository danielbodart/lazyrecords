package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Using;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.names;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static java.lang.String.format;

public class FromClause extends CompoundExpression{
    public FromClause(Definition table, Option<Join> join) {
        super(textOnly("from"), name(table).join(join.map(asExpression()).getOrElse(Expressions.empty())));
    }

    public static Function1<Join, Expression> asExpression() {
        return new Function1<Join, Expression>() {
            @Override
            public Expression call(Join join) throws Exception {
                return toSql(join);
            }
        };
    }

    public static Expression toSql(Join join) {
        Expressible records = (Expressible) join.records();
        SelectBuilder select = (SelectBuilder) records.express();
        Using using = (Using) join.using();
        return textOnly("inner join %s using %s", name(select.table()), names(using.keywords()));
    }

    public static Expression fromClause(Definition definition, Option<Join> join) {
        return new FromClause(definition, join);
    }

    public static Expression fromClause(Definition definition) {
        return new FromClause(definition, Option.<Join>none());
    }
}
