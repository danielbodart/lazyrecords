package com.googlecode.lazyrecords.sql.expressions;

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
