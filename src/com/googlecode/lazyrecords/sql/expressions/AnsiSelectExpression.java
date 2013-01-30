package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static java.lang.String.format;

public class AnsiSelectExpression extends CompoundExpression implements SelectExpression {
    private final Option<SetQuantifier> setQuantifier;
    private final SelectList selectList;
    private final FromClause fromClause;
    private final Option<WhereClause> whereClause;
    private final Option<OrderByClause> orderByClause;

    private AnsiSelectExpression(Option<SetQuantifier> setQuantifier,
                                 SelectList selectList,
                                 FromClause fromClause,
                                 Option<WhereClause> whereClause,
                                 Option<OrderByClause> orderByClause) {
        super(
                select,
                expression(setQuantifier),
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
        return new AnsiSelectExpression(setQuantifier,
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
