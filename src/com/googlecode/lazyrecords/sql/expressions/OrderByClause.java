package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.comparators.AscendingComparator;
import com.googlecode.totallylazy.comparators.CompositeComparator;
import com.googlecode.totallylazy.comparators.DescendingComparator;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class OrderByClause {
    public static Expression orderByClause(Option<Comparator<? super Record>> comparator) {
        return comparator.map(new Callable1<Comparator<? super Record>, Expression>() {
            public Expression call(Comparator<? super Record> comparator) throws Exception {
                return orderByClause(comparator);
            }
        }).getOrElse(Expressions.empty());
    }

    public static Expression orderByClause(Comparator<? super Record> comparator) {
        return textOnly("order by").join(toSql(comparator));
    }

    public static Expression toSql(Comparator<? super Record> comparator) {
        return new multi() { }.<Expression>methodOption(comparator).getOrThrow(new UnsupportedOperationException("Unsupported comparator " + comparator));
    }

    public static Expression toSql(AscendingComparator<? super Record, ?> comparator) {
        return SelectList.derivedColumn(comparator.callable()).join(textOnly("asc"));
    }

    public static Expression toSql(DescendingComparator<? super Record, ?> comparator) {
        return SelectList.derivedColumn(comparator.callable()).join(textOnly("desc"));
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
