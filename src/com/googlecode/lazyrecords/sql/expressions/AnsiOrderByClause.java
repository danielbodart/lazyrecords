package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.comparators.AscendingComparator;
import com.googlecode.totallylazy.comparators.CompositeComparator;
import com.googlecode.totallylazy.comparators.DescendingComparator;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class AnsiOrderByClause extends CompoundExpression implements OrderByClause {
    public AnsiOrderByClause(Comparator<? super Record> comparator) {
        super(orderBy.join(toSql(comparator)));
    }

    public static Option<OrderByClause> orderByClause(Option<Comparator<? super Record>> comparator) {
        return comparator.map(new Callable1<Comparator<? super Record>, OrderByClause>() {
            public OrderByClause call(Comparator<? super Record> comparator) throws Exception {
                return orderByClause(comparator);
            }
        });
    }

    public static OrderByClause orderByClause(Comparator<? super Record> comparator) {
        return new AnsiOrderByClause(comparator);
    }

    public static Expression toSql(Comparator<? super Record> comparator) {
        return new multi() { }.<Expression>methodOption(comparator).getOrThrow(new UnsupportedOperationException("Unsupported comparator " + comparator));
    }

    public static Expression toSql(AscendingComparator<? super Record, ?> comparator) {
        return Expressions.join(AnsiSelectList.valueExpression(comparator.callable()), textOnly("asc"));
    }

    public static Expression toSql(DescendingComparator<? super Record, ?> comparator) {
        return Expressions.join(AnsiSelectList.valueExpression(comparator.callable()), textOnly("desc"));
    }

    public static Expression toSql(CompositeComparator<Record> comparator) {
        Sequence<Comparator<? super Record>> comparators = comparator.comparators();
        return new CompoundExpression(comparators.map(toSql()), ", ");
    }

    public static Callable1<Comparator<? super Record>, Expression> toSql() {
        return new Callable1<Comparator<? super Record>, Expression>() {
            @Override
            public Expression call(Comparator<? super Record> comparator) throws Exception {
                return toSql(comparator);
            }
        };
    }
}
