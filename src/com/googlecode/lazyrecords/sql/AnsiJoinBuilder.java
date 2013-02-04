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
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectBuilder;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectList;
import com.googlecode.lazyrecords.sql.expressions.DerivedColumn;
import com.googlecode.lazyrecords.sql.expressions.Expressible;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.lazyrecords.sql.expressions.JoinCondition;
import com.googlecode.lazyrecords.sql.expressions.JoinSpecification;
import com.googlecode.lazyrecords.sql.expressions.JoinType;
import com.googlecode.lazyrecords.sql.expressions.NamedColumnsJoin;
import com.googlecode.lazyrecords.sql.expressions.SelectExpression;
import com.googlecode.lazyrecords.sql.expressions.SelectList;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unary;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectBuilder.from;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectList.selectList;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.columnReference;
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
        Sequence<DerivedColumn> selectList = grammar.selectList(columns).derivedColumns();
        final Map<String, List<DerivedColumn>> existingNames = existingNames(expression.selectList().derivedColumns());
        SelectExpression select = from(grammar, expression).select(selectList(selectList.map(lookup(existingNames)))).build();
        return new AnsiJoinBuilder(grammar, select);
    }

    private Unary<DerivedColumn> lookup(final Map<String, List<DerivedColumn>> existingNames) {
        return new Unary<DerivedColumn>() {
            @Override
            public DerivedColumn call(final DerivedColumn column) throws Exception {
                return existingNames.get(name(column)).get(0);
            }
        };
    }

    private Map<String, List<DerivedColumn>> existingNames(final Sequence<DerivedColumn> derivedColumns) {
        return derivedColumns.toMap(new Mapper<DerivedColumn, String>() {
            @Override
            public String call(final DerivedColumn column) throws Exception {
                return name(column);
            }
        });
    }

    private String name(final DerivedColumn column) {
        if(!column.asClause().isEmpty()) return column.asClause().get().alias();
        return DerivedColumn.methods.columnReferences(column).head().name();
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
        return expression;
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
