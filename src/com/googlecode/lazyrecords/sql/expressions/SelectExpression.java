package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.FromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.OrderByClause.orderByClause;
import static com.googlecode.lazyrecords.sql.expressions.SelectList.selectList;
import static com.googlecode.lazyrecords.sql.expressions.WhereClause.whereClause;

public class SelectExpression extends CompoundExpression {
    public static final TextOnlyExpression SELECT = textOnly("select");

    private SelectExpression(final SetQuantifier setQuantifier, final Sequence<Keyword<?>> select, final Definition table, final Option<Predicate<? super Record>> where, final Option<Comparator<? super Record>> sort, Option<? extends Join> join) {
        super(
                querySpecification(setQuantifier, select),
                fromClause(table, join),
                whereClause(where),
                orderByClause(sort)
        );
    }

    public static SelectExpression selectExpression(final Definition table, final Sequence<Keyword<?>> select, final SetQuantifier setQuantifier, final Option<Predicate<? super Record>> where, final Option<Comparator<? super Record>> sort, Option<? extends Join> join) {
        return new SelectExpression(setQuantifier, select, table, where, sort, join);
    }

    public static Expression querySpecification(SetQuantifier setQuantifier, final Sequence<Keyword<?>> select) {
        return Expressions.join(SELECT, SetQuantifier.setQuantifier(setQuantifier), selectList(select));
    }
}
