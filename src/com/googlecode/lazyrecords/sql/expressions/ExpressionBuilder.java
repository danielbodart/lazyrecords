package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;

public interface ExpressionBuilder extends Expressible, Expression {
    Sequence<Keyword<?>> fields();

    ExpressionBuilder select(Keyword<?>... columns);

    ExpressionBuilder select(Sequence<? extends Keyword<?>> columns);

    ExpressionBuilder filter(Predicate<? super Record> predicate);

    ExpressionBuilder orderBy(Comparator<? super Record> comparator);

    ExpressionBuilder groupBy(Keyword<?>... columns);

    ExpressionBuilder groupBy(Sequence<? extends Keyword<?>> columns);

    ExpressionBuilder fetch(int number);

    ExpressionBuilder count();

    ExpressionBuilder distinct();

    ExpressionBuilder reduce(Reducer<?, ?> reducer);

    ExpressionBuilder join(Join join);
}
