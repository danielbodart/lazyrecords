package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface GroupByClause extends Expression {

    TextOnlyExpression groupBy = textOnly("group by");
    Sequence<DerivedColumn> groups();

    class functions {
        public static Function1<GroupByClause, Sequence<DerivedColumn>> groups = new Function1<GroupByClause, Sequence<DerivedColumn>>() {
            @Override
            public Sequence<DerivedColumn> call(GroupByClause groupByClause) throws Exception {
                return groupByClause.groups();
            }
        };
    }

}
