package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
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
        if (comparator instanceof AscendingComparator) {
            return SelectList.derivedColumn(Unchecked.<AscendingComparator<? super Record, ?>>cast(comparator).callable()).join(textOnly("asc"));
        }
        if (comparator instanceof DescendingComparator) {
            return SelectList.derivedColumn(Unchecked.<DescendingComparator<? super Record, ?>>cast(comparator).callable()).join(textOnly("desc"));
        }
        if (comparator instanceof CompositeComparator) {
			Sequence<Comparator<? super Record>> comparators = Unchecked.<CompositeComparator<Record>>cast(comparator).comparators();
			return new CompoundExpression(comparators.map(toSql()), ", ");
        }
        throw new UnsupportedOperationException("Unsupported comparator " + comparator);
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
