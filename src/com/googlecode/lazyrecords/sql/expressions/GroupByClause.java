package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public interface GroupByClause extends Expression {

    TextOnlyExpression groupBy = textOnly("group by");
    Sequence<ValueExpression> groups();

    class functions {
        public static Function1<GroupByClause, Sequence<ValueExpression>> groups = new Function1<GroupByClause, Sequence<ValueExpression>>() {
            @Override
            public Sequence<ValueExpression> call(GroupByClause groupByClause) throws Exception {
                return groupByClause.groups();
            }
        };
    }

}
