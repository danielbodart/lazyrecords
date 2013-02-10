package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.comparators.AscendingComparator;
import com.googlecode.totallylazy.comparators.CompositeComparator;
import com.googlecode.totallylazy.comparators.DescendingComparator;
import com.googlecode.totallylazy.multi;

import java.util.Comparator;

import static com.googlecode.totallylazy.Sequences.one;

public class AnsiSortSpecification extends CompoundExpression implements SortSpecification {
    private final ValueExpression sortKey;
    private final OrderingSpecification orderingSpecification;

    private AnsiSortSpecification(ValueExpression sortKey, OrderingSpecification orderingSpecification) {
        super(sortKey, orderingSpecification);
        this.sortKey = sortKey;
        this.orderingSpecification = orderingSpecification;
    }

    public static SortSpecification sortSpecification(ValueExpression sortKey, OrderingSpecification orderingSpecification) {
        return new AnsiSortSpecification(sortKey, orderingSpecification);
    }

    @Override
    public ValueExpression sortKey() {
        return sortKey;
    }

    @Override
    public OrderingSpecification orderingSpecification() {
        return orderingSpecification;
    }
}
