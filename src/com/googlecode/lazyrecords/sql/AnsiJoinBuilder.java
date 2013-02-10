package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.InnerJoin;
import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Joiner;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.On;
import com.googlecode.lazyrecords.OuterJoin;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Using;
import com.googlecode.lazyrecords.sql.expressions.AnsiJoinType;
import com.googlecode.lazyrecords.sql.expressions.DerivedColumn;
import com.googlecode.lazyrecords.sql.expressions.Expressible;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.lazyrecords.sql.expressions.JoinCondition;
import com.googlecode.lazyrecords.sql.expressions.JoinSpecification;
import com.googlecode.lazyrecords.sql.expressions.JoinType;
import com.googlecode.lazyrecords.sql.expressions.NamedColumnsJoin;
import com.googlecode.lazyrecords.sql.expressions.OrderByClause;
import com.googlecode.lazyrecords.sql.expressions.Qualifier;
import com.googlecode.lazyrecords.sql.expressions.SelectExpression;
import com.googlecode.lazyrecords.sql.expressions.SelectList;
import com.googlecode.lazyrecords.sql.expressions.WhereClause;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectBuilder.from;
import static com.googlecode.lazyrecords.sql.expressions.DerivedColumn.methods.columnReferences;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.columnReference;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.aggregates;
import static com.googlecode.totallylazy.Sequences.sequence;

public class AnsiJoinBuilder implements ExpressionBuilder {
    private final SqlGrammar grammar;
    private final SelectExpression expression;

    private AnsiJoinBuilder(final SqlGrammar grammar, final SelectExpression expression) {
        this.grammar = grammar;
        this.expression = expression;
    }

    private static AnsiJoinBuilder join(final SelectExpression expression) {
        return new AnsiJoinBuilder(new AnsiSqlGrammar(), expression);
    }

    public static AnsiJoinBuilder join(ExpressionBuilder primary, ExpressionBuilder secondary, JoinType type, JoinSpecification specification) {
        if (primary instanceof AnsiJoinBuilder) {
            AnsiJoinBuilder builder = (AnsiJoinBuilder) primary;
            return join(Merger.merger(builder.expression, (SelectExpression) secondary.build(), type, specification).merge());
        }
        return join(Merger.merger((SelectExpression) primary.build(), (SelectExpression) secondary.build(), type, specification).merge());
    }

    public static ExpressionBuilder join(final ExpressionBuilder builder, final Join join) {
        ExpressionBuilder secondary = (ExpressionBuilder) ((Expressible) join.records()).build();
        JoinType type = joinType(join);
        JoinSpecification specification = joinSpecification(join.joiner());
        return join(builder, secondary, type, specification);
    }

    private static JoinSpecification joinSpecification(final Joiner joiner) {
        if (joiner instanceof On<?>)
            return JoinCondition.joinCondition(columnReference(((On) joiner).left()), columnReference(((On) joiner).right()));
        if (joiner instanceof Using)
            return NamedColumnsJoin.namedColumnsJoin(((Using) joiner).keywords().map(Keyword.functions.name));
        throw new UnsupportedOperationException();
    }

    private static JoinType joinType(final Join join) {
        if (join instanceof InnerJoin) return AnsiJoinType.inner;
        if (join instanceof OuterJoin) return AnsiJoinType.left;
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return SelectList.methods.fields(expression.selectList());
    }

    @Override
    public ExpressionBuilder select(Keyword<?>... columns) {
        return select(sequence(columns));
    }

    @Override
    public ExpressionBuilder select(Sequence<? extends Keyword<?>> columns) {
        return select(grammar.selectList(columns));
    }

    public ExpressionBuilder select(final SelectList selectList) {
        return builder(from(grammar, expression).select(qualifier().qualify(selectList)).build());
    }

    public ExpressionBuilder builder(final SelectExpression select) {
        return new AnsiJoinBuilder(grammar, select);
    }

    @Override
    public ExpressionBuilder filter(Predicate<? super Record> predicate) {
        return filter(grammar.whereClause(predicate));
    }

    public ExpressionBuilder filter(final WhereClause whereClause) {
        return builder(from(grammar, expression).filter(qualifier().qualify(whereClause)).build());
    }

    @Override
    public ExpressionBuilder orderBy(Comparator<? super Record> comparator) {
        return orderBy(grammar.orderByClause(comparator));
    }

    public ExpressionBuilder orderBy(final OrderByClause orderByClause) {
        return builder(from(grammar, expression).orderBy(qualifier().qualify(orderByClause)).build());
    }

    @Override
    public ExpressionBuilder count() {
        return builder(from(grammar, expression).count().build());
    }

    @Override
    public ExpressionBuilder distinct() {
        return builder(from(grammar, expression).distinct().build());
    }

    @Override
    public ExpressionBuilder reduce(Reducer<?, ?> reducer) {
        return select(aggregates(reducer, fields()));
    }

    @Override
    public SelectExpression build() {
        return expression;
    }

    @Override
    public String text() {
        return expression.text();
    }

    @Override
    public Sequence<Object> parameters() {
        return expression.parameters();
    }

    private Qualifier qualifier() {
        return Qualifier.qualifier("ignored", lookup(existingQualifiers()));
    }

    private Mapper<String, String> lookup(final Map<String, String> existingNames) {
        return new Mapper<String, String>() {
            @Override
            public String call(final String column) throws Exception {
                return existingNames.get(column);
            }
        };
    }

    private Map<String, String> existingQualifiers() {
        return Maps.mapValues(expression.selectList().derivedColumns().toMap(name()), new Mapper<List<DerivedColumn>, String>() {
            @Override
            public String call(final List<DerivedColumn> columns) throws Exception {
                return columnReferences(columns.get(0)).head().qualifier().get();
            }
        });
    }

    private Mapper<DerivedColumn, String> name() {
        return new Mapper<DerivedColumn, String>() {
            @Override
            public String call(final DerivedColumn column) throws Exception {
                return name(column);
            }
        };
    }

    private String name(final DerivedColumn column) {
        if(!column.asClause().isEmpty()) return column.asClause().get().alias();
        return columnReferences(column).head().name();
    }
}
