package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;

import static com.googlecode.totallylazy.Sequences.one;

public class JoinBuilder implements ExpressionBuilder {
    private final ExpressionBuilder primary;
    private final Sequence<ExpressionBuilder> joins;

    public JoinBuilder(ExpressionBuilder primary, Sequence<ExpressionBuilder> joins) {
        this.primary = primary;
        this.joins = joins;
    }

    public static ExpressionBuilder join(ExpressionBuilder left, ExpressionBuilder right) {
        if(left instanceof JoinBuilder) {
            JoinBuilder joinBuilder = (JoinBuilder) left;
            return new JoinBuilder(joinBuilder.primary, joinBuilder.joins.add(right));
        }
        return new JoinBuilder(left, one(right));
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return primary.fields().join(joins.flatMap(new Mapper<ExpressionBuilder, Sequence<Keyword<?>>>() {
            @Override
            public Sequence<Keyword<?>> call(ExpressionBuilder expressionBuilder) throws Exception {
                return expressionBuilder.fields();
            }
        })).unique();
    }

    @Override
    public ExpressionBuilder select(Keyword<?>... columns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder select(Sequence<Keyword<?>> columns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder where(Predicate<? super Record> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder orderBy(Comparator<? super Record> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder distinct() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder reduce(Reducer<?, ?> reducer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression build() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String text() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<Object> parameters() {
        throw new UnsupportedOperationException();
    }

}
