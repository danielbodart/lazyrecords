package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.*;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.*;
import static com.googlecode.lazyrecords.sql.expressions.SelectList.derivedColumn;
import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.Sequences.sequence;

public class WhereClause extends CompoundExpression {
    private WhereClause(Predicate<? super Record> predicate) {
        super(prefixWhere(toSql(predicate)));
    }

    private static Expression prefixWhere(Expression expression) {
        if(Expressions.isEmpty(expression))return expression;
        return textOnly("where").join(expression);
    }

    public static WhereClause whereClause(Predicate<? super Record> predicate) {
        return new WhereClause(predicate);
    }

    public static Expression whereClause(Option<? extends Predicate<? super Record>> predicate) {
        return predicate.map(whereClause()).getOrElse(empty());
    }

    public static Function1<Predicate<? super Record>, Expression> whereClause() {
        return new Function1<Predicate<? super Record>, Expression>() {
            public Expression call(Predicate<? super Record> predicate) throws Exception {
                return whereClause(predicate);
            }
        };
    }

    public static Expression toSql(Predicate<?> predicate) {
        return new multi() { }.<Expression>methodOption(predicate).getOrThrow(new UnsupportedOperationException());
    }

    public static Expression toSql(AlwaysTrue predicate) {
        return textOnly("1 = 1");
    }

    public static Expression toSql(AlwaysFalse predicate) {
        return textOnly("1 != 1");
    }

    public static Expression toSql(WherePredicate<Record, ?> predicate) {
        Expression predicateSql = toSql(predicate.predicate());
        return derivedColumn(predicate.callable()).join(predicateSql);
    }

    public static Expression toSql(AndPredicate<?> predicate) {
        if (predicate.predicates().isEmpty()) return empty();
        return Expressions.join(toExpressions(predicate.predicates()), "(", " and ", ")");
    }

    public static Expression toSql(OrPredicate<?> predicate) {
        if (predicate.predicates().isEmpty()) return empty();
        if (!predicate.predicates().safeCast(AlwaysTrue.class).isEmpty()) return empty();
        return Expressions.join(toExpressions(predicate.predicates()), "(", " or ", ")");
    }

    public static Expression toSql(NullPredicate<?> predicate) {
        return textOnly("is null");
    }

    public static Expression toSql(EqualsPredicate<?> predicate) {
        return expression("= ?", predicate.value());
    }

    public static Expression toSql(Not<?> predicate) {
        return textOnly("not (").join(toSql(predicate.predicate())).join(textOnly(")"));
    }

    public static Expression toSql(GreaterThan<?> predicate) {
        return expression("> ?", predicate.value());
    }

    public static Expression toSql(GreaterThanOrEqualTo<?> predicate) {
        return expression(">= ?", predicate.value());
    }

    public static Expression toSql(LessThan<?> predicate) {
        return expression("< ?", predicate.value());
    }

    public static Expression toSql(LessThanOrEqualTo<?> predicate) {
        return expression("<= ?", predicate.value());
    }

    public static Expression toSql(Between<?> predicate) {
        return expression("between ? and ?", sequence(predicate.lower(), predicate.upper()));
    }

    public static Expression toSql(InPredicate<?> predicate) {
        Sequence<Object> sequence = sequence(predicate.values());
        if (sequence instanceof Expressible) {
            Expression pair = ((Expressible) sequence).build();
            return expression("in ( " + pair.text() + ")", pair.parameters());
        }
        return expression(repeat("?").take(sequence.size()).toString("in (", ",", ")"), sequence);
    }

    public static Expression toSql(StartsWithPredicate predicate) {
        return expression("like ?", sequence(predicate.value() + "%"));
    }

    public static Expression toSql(EndsWithPredicate predicate) {
        return expression("like ?", sequence("%" + predicate.value()));
    }

    public static Expression toSql(ContainsPredicate predicate) {
        return expression("like ?", sequence("%" + predicate.value() + "%"));
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

}
