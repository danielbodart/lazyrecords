package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.grammars.AndExpression;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import java.util.Comparator;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectExpression.selectExpression;
import static com.googlecode.lazyrecords.sql.expressions.DerivedColumn.methods.columnReferences;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.aggregates;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.countStar;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;

public class AnsiSelectBuilder implements ExpressionBuilder {
    private final SqlGrammar grammar;
    private final SelectExpression expression;

    private AnsiSelectBuilder(final SqlGrammar grammar, final SelectExpression expression) {
        this.grammar = grammar;
        this.expression = expression;
    }

    public static AnsiSelectBuilder from(final SqlGrammar grammar, final SelectExpression expression) {
        return new AnsiSelectBuilder(grammar, expression);
    }

    public static AnsiSelectBuilder from(final SqlGrammar grammar, final Definition definition) {
        return new AnsiSelectBuilder(grammar, grammar.selectExpression(
                Option.<SetQuantifier>none(),
                definition.fields(),
                definition,
                Option.<Predicate<? super Record>>none(),
                Option.<Comparator<? super Record>>none()
        ));
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return expression.selectList().derivedColumns().flatMap(new Mapper<DerivedColumn, Sequence<Keyword<?>>>() {
            @Override
            public Sequence<Keyword<?>> call(final DerivedColumn column) throws Exception {
                if (!column.asClause().isEmpty())
                    return Sequences.<Keyword<?>>one(keyword(removeQuotes(column.asClause().get().alias()), column.forClass()));
                return columnReferences(column).map(asKeyword(column));
            }
        });
    }

    private Mapper<ColumnReference, Keyword<?>> asKeyword(final DerivedColumn column) {
        return new Mapper<ColumnReference, Keyword<?>>() {
            @Override
            public Keyword<?> call(final ColumnReference columnReference) throws Exception {
                return keyword(removeQuotes(columnReference.name()), column.forClass());
            }
        };
    }

    public static String removeQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"")) s = s.substring(1);
        if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
        return s;
    }

    @Override
    public ExpressionBuilder select(final Keyword<?>... columns) {
        return select(sequence(columns));
    }

    @Override
    public ExpressionBuilder select(final Sequence<? extends Keyword<?>> columns) {
        return select(grammar.selectList(columns));
    }

    public ExpressionBuilder select(final SelectList selectList) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                selectList,
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause()));
    }

    @Override
    public ExpressionBuilder filter(final Predicate<? super Record> predicate) {
        return filter(grammar.whereClause(predicate));
    }

    public ExpressionBuilder filter(final WhereClause whereClause) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                some(combine(expression.whereClause(), whereClause)),
                expression.orderByClause()));
    }

    private WhereClause combine(final Option<WhereClause> existing, final WhereClause additional) {
        if (existing.isEmpty()) return additional;
        return AnsiWhereClause.whereClause(AndExpression.andExpression(existing.get().expression(), additional.expression()));
    }

    @Override
    public ExpressionBuilder orderBy(final Comparator<? super Record> comparator) {
        return orderBy(grammar.orderByClause(comparator));
    }

    public ExpressionBuilder orderBy(final OrderByClause orderByClause) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                some(orderByClause)));
    }

    @Override
    public ExpressionBuilder count() {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                grammar.selectList(countStar()),
                expression.fromClause(),
                expression.whereClause(),
                Option.<OrderByClause>none()));
    }

    @Override
    public ExpressionBuilder distinct() {
        return from(grammar, selectExpression(
                Option.<SetQuantifier>some(AnsiSetQuantifier.DISTINCT),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause()));
    }

    @Override
    public ExpressionBuilder reduce(final Reducer<?, ?> reducer) {
        return select(aggregates(reducer, fields()));
    }

    @Override
    public Expression build() {
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
}
