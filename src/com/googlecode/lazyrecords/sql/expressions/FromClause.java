package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.*;
import static java.lang.String.format;

public class FromClause extends CompoundExpression{
    public FromClause(Definition table, Option<? extends Join> join) {
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
        return textOnly("%s %s using %s", joinExpression(join),name(select.table()), names(using.keywords()));
    }

	private static String joinExpression(Join join) {
		if(join instanceof InnerJoin)
			return "inner join";
		if(join instanceof LeftJoin)
			return "left join";
		throw new UnsupportedOperationException(format("Cannot created join expression for %s", join));
	}

	public static Expression fromClause(Definition definition, Option<? extends Join> join) {
        return new FromClause(definition, join);
    }

    public static Expression fromClause(Definition definition) {
        return new FromClause(definition, Option.<Join>none());
    }
}
