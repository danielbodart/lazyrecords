package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.FromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.OrderByClause.orderByClause;
import static com.googlecode.lazyrecords.sql.expressions.SelectList.selectList;
import static com.googlecode.lazyrecords.sql.expressions.WhereClause.whereClause;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class SelectExpression extends CompoundExpression {
    public static final TextOnlyExpression SELECT = textOnly("select");

    private SelectExpression(final SetQuantifier setQuantifier, final Sequence<Keyword<?>> select, final Definition table, final Option<Predicate<? super Record>> where, final Option<Comparator<? super Record>> sort, Iterable<? extends Join> joins) {
        super(
                querySpecification(setQuantifier, select),
                fromClause(table, joins),
                whereClause(where),
                orderByClause(sort)
        );
    }

    public static SelectExpression selectExpression(final Definition table, final Sequence<Keyword<?>> select, final SetQuantifier setQuantifier, final Option<Predicate<? super Record>> where, final Option<Comparator<? super Record>> sort, Iterable<? extends Join> joins) {
        return new SelectExpression(setQuantifier, select, table, where, sort, joins);
    }

    public static Expression querySpecification(SetQuantifier setQuantifier, final Sequence<Keyword<?>> select) {
        return Expressions.join(SELECT, SetQuantifier.setQuantifier(setQuantifier),selectList(select));
    }

    public static String tableAlias(Number index) {
        return format("t%s", index);
    }



}
