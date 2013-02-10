package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.grammars.AndExpression;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectExpression.selectExpression;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSetQuantifier.DISTINCT;
import static com.googlecode.lazyrecords.sql.expressions.AnsiWhereClause.whereClause;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.aggregates;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.countStar;
import static com.googlecode.lazyrecords.sql.grammars.AndExpression.andExpression;
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
        return SelectList.methods.fields(expression.selectList());
    }

    @Override
    public AnsiSelectBuilder select(final Keyword<?>... columns) {
        return select(sequence(columns));
    }

    @Override
    public AnsiSelectBuilder select(final Sequence<? extends Keyword<?>> columns) {
        return select(grammar.selectList(columns));
    }

    public AnsiSelectBuilder select(final SelectList selectList) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                selectList,
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause()));
    }

    @Override
    public AnsiSelectBuilder filter(final Predicate<? super Record> predicate) {
        return filter(grammar.whereClause(predicate));
    }

    public AnsiSelectBuilder filter(final WhereClause whereClause) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                some(combine(expression.whereClause(), whereClause)),
                expression.orderByClause()));
    }

    private WhereClause combine(final Option<WhereClause> existing, final WhereClause additional) {
        if (existing.isEmpty()) return additional;
        return whereClause(andExpression(existing.get().expression(), additional.expression()));
    }

    @Override
    public AnsiSelectBuilder orderBy(final Comparator<? super Record> comparator) {
        return orderBy(grammar.orderByClause(comparator));
    }

    public AnsiSelectBuilder orderBy(final OrderByClause orderByClause) {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                some(orderByClause)));
    }

    @Override
    public AnsiSelectBuilder count() {
        return from(grammar, selectExpression(
                expression.setQuantifier(),
                grammar.selectList(countStar()),
                expression.fromClause(),
                expression.whereClause(),
                Option.<OrderByClause>none()));
    }

    @Override
    public AnsiSelectBuilder distinct() {
        return from(grammar, selectExpression(
                Option.<SetQuantifier>some(DISTINCT),
                expression.selectList(),
                expression.fromClause(),
                expression.whereClause(),
                expression.orderByClause()));
    }

    @Override
    public AnsiSelectBuilder reduce(final Reducer<?, ?> reducer) {
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
}
