package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.GroupByClause.groupBy;
import static com.googlecode.totallylazy.Sequences.cons;

public class AnsiGroupByClause extends CompoundExpression implements GroupByClause {
    private final Sequence<ValueExpression> groups;

    private AnsiGroupByClause(Sequence<ValueExpression> groups) {
        super(cons(groupBy, parts(groups)));
        this.groups = groups;
    }

    private static Sequence<? extends Expression> parts(Sequence<? extends Expression> groups) {
        return groups.safeCast(Expression.class).intersperse(textOnly(", "));
    }

    public static AnsiGroupByClause groupByClause(Sequence<ValueExpression> groups) {
        return new AnsiGroupByClause(groups);
    }

    @Override
    public Sequence<ValueExpression> groups() {
        return groups;
    }

    public static class functions{
        public static Function1<Sequence<ValueExpression>, GroupByClause> groupByClause = new Function1<Sequence<ValueExpression>, GroupByClause>() {
            @Override
            public GroupByClause call(Sequence<ValueExpression> groups) throws Exception {
                return AnsiGroupByClause.groupByClause(groups);
            }
        };
    }
}
