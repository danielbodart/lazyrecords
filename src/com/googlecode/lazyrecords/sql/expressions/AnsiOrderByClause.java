package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Sequences.cons;

public class AnsiOrderByClause extends CompoundExpression implements OrderByClause {
    private final Sequence<SortSpecification> sortSpecifications;

    private AnsiOrderByClause(Sequence<SortSpecification> sortSpecifications) {
        super(cons(orderBy, parts(sortSpecifications)));
        this.sortSpecifications = sortSpecifications;
    }

    private static Sequence<? extends Expression> parts(Sequence<? extends Expression> sortSpecifications) {
        return sortSpecifications.safeCast(Expression.class).intersperse(textOnly(", "));
    }

    public static AnsiOrderByClause orderByClause(Sequence<SortSpecification> sortSpecifications) {
        return new AnsiOrderByClause(sortSpecifications);
    }

    @Override
    public Sequence<SortSpecification> sortSpecifications() {
        return sortSpecifications;
    }
}
