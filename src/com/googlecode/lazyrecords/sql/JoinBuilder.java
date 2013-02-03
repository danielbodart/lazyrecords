package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Joiner;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.AnsiFromClause;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectExpression;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectList;
import com.googlecode.lazyrecords.sql.expressions.DerivedColumn;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.lazyrecords.sql.expressions.FromClause;
import com.googlecode.lazyrecords.sql.expressions.Qualifier;
import com.googlecode.lazyrecords.sql.expressions.SelectBuilder;
import com.googlecode.lazyrecords.sql.expressions.SelectExpression;
import com.googlecode.lazyrecords.sql.expressions.SelectList;
import com.googlecode.lazyrecords.sql.expressions.TableReference;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.multi;

import java.util.Comparator;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.one;

public class JoinBuilder implements ExpressionBuilder {
    private final SelectBuilder primary;
    private final Sequence<Pair<ExpressionBuilder, Joiner>> secondaries;

    private JoinBuilder(SelectBuilder primary, Sequence<Pair<ExpressionBuilder, Joiner>> secondaries) {
        this.primary = primary;
        this.secondaries = secondaries;
    }

    public static JoinBuilder join(ExpressionBuilder primary, ExpressionBuilder secondary, Joiner joiner) {
        return new multi() {
        }.method(primary, secondary, joiner);
    }

    public static JoinBuilder join(SelectBuilder builder, ExpressionBuilder secondary, Joiner joiner) {
        return new JoinBuilder(builder, one(pair(secondary, joiner)));
    }

    public static JoinBuilder join(JoinBuilder builder, ExpressionBuilder secondary, Joiner joiner) {
        return new JoinBuilder(builder.primary, builder.secondaries.add(pair(secondary, joiner)));
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return primary.fields().join(secondaries.flatMap(new Mapper<Pair<ExpressionBuilder, Joiner>, Sequence<Keyword<?>>>() {
            @Override
            public Sequence<Keyword<?>> call(final Pair<ExpressionBuilder, Joiner> pair) throws Exception {
                return pair.first().fields();
            }
        })).reverse().unique();
    }

    @Override
    public ExpressionBuilder select(Keyword<?>... columns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder select(Sequence<? extends Keyword<?>> columns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder filter(Predicate<? super Record> predicate) {
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
    public SelectExpression build() {
        SelectExpression select = new Qualifier("p").qualify(primary.build());
        Sequence<SelectExpression> expressions = secondaries.map(first(ExpressionBuilder.class)).
                map(selectExpression).
                zipWithIndex().
                map(qualify).
                realise();
        return AnsiSelectExpression.selectExpression(
                select.setQuantifier(),
                mergeSelectList(select, expressions),
                mergeFromClause(select, expressions),
                select.whereClause(),
                select.orderByClause());
    }

    private FromClause mergeFromClause(final SelectExpression select, final Sequence<SelectExpression> expressions) {
        Sequence<TableReference> map = expressions.map(tableReference);
        return select.fromClause();
    }

    private static final Mapper<SelectExpression,TableReference> tableReference = new Mapper<SelectExpression, TableReference>() {
        @Override
        public TableReference call(final SelectExpression expression) throws Exception {
            return expression.fromClause().tableReference();
        }
    };


    private static SelectList mergeSelectList(final SelectExpression select, final Sequence<SelectExpression> expressions) {
        return AnsiSelectList.selectList(select.selectList().derivedColumns().join(expressions.flatMap(derivedColumns)));
    }

    private static final Mapper<SelectExpression, Sequence<DerivedColumn>> derivedColumns = new Mapper<SelectExpression, Sequence<DerivedColumn>>() {
        @Override
        public Sequence<DerivedColumn> call(final SelectExpression expression) throws Exception {
            return expression.selectList().derivedColumns();
        }
    };

    private static final Mapper<Pair<Number, SelectExpression>, SelectExpression> qualify = new Mapper<Pair<Number, SelectExpression>, SelectExpression>() {
        @Override
        public SelectExpression call(final Pair<Number, SelectExpression> pair) throws Exception {
            return new Qualifier("s" + pair.first()).qualify(pair.second());
        }
    };

    private static final Mapper<ExpressionBuilder, SelectExpression> selectExpression = new Mapper<ExpressionBuilder, SelectExpression>() {
        @Override
        public SelectExpression call(final ExpressionBuilder expressionBuilder) throws Exception {
            return (SelectExpression) expressionBuilder.build();
        }
    };

    @Override
    public String text() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<Object> parameters() {
        throw new UnsupportedOperationException();
    }

}
