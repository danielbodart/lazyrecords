package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.*;

import static com.googlecode.lazyrecords.sql.expressions.AnsiValueExpression.valueExpression;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.empty;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.Sequences.sequence;

public class AnsiWhereClause extends CompoundExpression implements WhereClause {
    private final Expression expression;

    public AnsiWhereClause(Expression expression) {
        super(prefixWhere(expression));
        this.expression = expression;
    }

    private static Expression prefixWhere(Expression expression) {
        if (Expressions.isEmpty(expression)) return expression;
        return where.join(expression);
    }

    public static WhereClause whereClause(Predicate<? super Record> predicate) {
        return whereClause(toSql(predicate));
    }

    public static WhereClause whereClause(Expression expression) {
        return new AnsiWhereClause(expression);
    }

    public static Option<WhereClause> whereClause(Option<? extends Predicate<? super Record>> predicate) {
        return predicate.map(whereClause());
    }

    public static Mapper<Predicate<? super Record>, WhereClause> whereClause() {
        return new Mapper<Predicate<? super Record>, WhereClause>() {
            public WhereClause call(Predicate<? super Record> predicate) throws Exception {
                return whereClause(predicate);
            }
        };
    }

    public static Expression toSql(Predicate<?> predicate) { return new multi() {}.<Expression>methodOption(predicate).getOrThrow(new UnsupportedOperationException()); }
    public static Expression toSql(AlwaysTrue predicate) { return textOnly("1 = 1"); }
    public static Expression toSql(AlwaysFalse predicate) { return textOnly("1 != 1"); }

    public static Expression toSql(AndPredicate<?> predicate) {
        if (predicate.predicates().isEmpty()) return empty();
        return Expressions.join(toExpressions(predicate.predicates()), "(", " and ", ")");
    }

    public static Expression toSql(OrPredicate<?> predicate) {
        if (predicate.predicates().isEmpty()) return empty();
        if (!predicate.predicates().safeCast(AlwaysTrue.class).isEmpty()) return empty();
        return Expressions.join(toExpressions(predicate.predicates()), "(", " or ", ")");
    }

    public static Expression toSql(Not<?> predicate) {
        return textOnly("not (").join(toSql(predicate.predicate())).join(textOnly(")"));
    }

    public static PredicateExpression toSql(WherePredicate<Record, ?> predicate) {
        ValueExpression valueExpression = valueExpression(predicate.callable());
        Expression predicateSql = toSql(predicate.predicate());
        return AnsiPredicateExpression.predicateExpression(valueExpression, predicateSql);
    }

    public static Expression toSql(NullPredicate<?> predicate) {
        return textOnly("is null");
    }

    public static Expression toSql(EqualsPredicate<?> predicate) {
        return Expressions.expression("= ?", predicate.value());
    }

    public static Expression toSql(GreaterThan<?> predicate) {
        return Expressions.expression("> ?", predicate.value());
    }

    public static Expression toSql(GreaterThanOrEqualTo<?> predicate) {
        return Expressions.expression(">= ?", predicate.value());
    }

    public static Expression toSql(LessThan<?> predicate) {
        return Expressions.expression("< ?", predicate.value());
    }

    public static Expression toSql(LessThanOrEqualTo<?> predicate) {
        return Expressions.expression("<= ?", predicate.value());
    }

    public static Expression toSql(Between<?> predicate) {
        return Expressions.expression("between ? and ?", sequence(predicate.lower(), predicate.upper()));
    }

    public static Expression toSql(InPredicate<?> predicate) {
        Sequence<Object> sequence = sequence(predicate.values());
        if (sequence instanceof Expressible) {
            Expression pair = ((Expressible) sequence).build();
            return Expressions.expression("in ( " + pair.text() + ")", pair.parameters());
        }
        return Expressions.expression(repeat("?").take(sequence.size()).toString("in (", ",", ")"), sequence);
    }

    public static Expression toSql(StartsWithPredicate predicate) {
        return Expressions.expression("like ?", sequence(predicate.value() + "%"));
    }

    public static Expression toSql(EndsWithPredicate predicate) {
        return Expressions.expression("like ?", sequence("%" + predicate.value()));
    }

    public static Expression toSql(ContainsPredicate predicate) {
        return Expressions.expression("like ?", sequence("%" + predicate.value() + "%"));
    }

    private static Sequence<Expression> toExpressions(Sequence<? extends Predicate<?>> predicates) {
        return predicates.map(toSql());
    }

    public static Function1<Predicate<?>, Expression> toSql() {
        return new Function1<Predicate<?>, Expression>() {
            public Expression call(Predicate<?> predicate) throws Exception {
                return toSql(predicate);
            }
        };
    }

    @Override
    public Expression expression() {
        return expression;
    }
}
