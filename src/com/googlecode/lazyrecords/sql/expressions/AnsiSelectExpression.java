package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.predicates.LogicalPredicate;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.text;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;
import static java.lang.String.format;

public class AnsiSelectExpression extends CompoundExpression implements SelectExpression {
    protected final Option<SetQuantifier> setQuantifier;
    protected final SelectList selectList;
    protected final FromClause fromClause;
    protected final Option<WhereClause> whereClause;
    protected final Option<OrderByClause> orderByClause;

    protected AnsiSelectExpression(Option<SetQuantifier> setQuantifier,
                                 SelectList selectList,
                                 FromClause fromClause,
                                 Option<WhereClause> whereClause,
                                 Option<OrderByClause> orderByClause) {
        super(
                setQuantifier.isEmpty() ? select : select.join(expression(setQuantifier)),
                selectList,
                fromClause,
                expression(whereClause),
                expression(orderByClause)
        );
        this.setQuantifier = setQuantifier;
        this.selectList = selectList;
        this.fromClause = fromClause;
        this.whereClause = whereClause;
        this.orderByClause = orderByClause;
    }

    public static SelectExpression selectExpression(Option<SetQuantifier> setQuantifier,
                                                    SelectList selectList,
                                                    FromClause fromClause,
                                                    Option<WhereClause> whereClause,
                                                    Option<OrderByClause> orderByClause) {
        return new AnsiSelectExpression(setQuantifier.filter(where(Expressions.text(), not(equalIgnoringCase("all")))),
                selectList,
                fromClause,
                whereClause,
                orderByClause);
    }

    public static String tableAlias(Number index) {
        return format("t%s", index);
    }

    @Override
    public Option<SetQuantifier> setQuantifier() {
        return setQuantifier;
    }

    @Override
    public SelectList selectList() {
        return selectList;
    }

    @Override
    public FromClause fromClause() {
        return fromClause;
    }

    @Override
    public Option<WhereClause> whereClause() {
        return whereClause;
    }

    @Override
    public Option<OrderByClause> orderByClause() {
        return orderByClause;
    }
}
