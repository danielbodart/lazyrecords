package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.*;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.*;
import static com.googlecode.lazyrecords.sql.expressions.SelectList.derivedColumn;
import static com.googlecode.totallylazy.Sequences.repeat;
import static com.googlecode.totallylazy.Sequences.sequence;

public class WhereClause extends CompoundExpression{
    private WhereClause(Predicate<? super Record> predicate) {
        super(prefixWhere(toSql(predicate)));
    }

    private static Expression prefixWhere(Expression expression) {
        if(Expressions.isEmpty(expression)){
            return expression;
        }
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
        if(predicate instanceof AllPredicate){
            return empty();
        }
        if (predicate instanceof WherePredicate) {
            WherePredicate<Record,?> wherePredicate = Unchecked.cast(predicate);
			if(wherePredicate.predicate() instanceof AllPredicate)
				return empty();
            Callable1<? super Record, ?> callable = wherePredicate.callable();
            return derivedColumn(callable).join(toSql(wherePredicate.predicate()));
        }
        if (predicate instanceof AndPredicate) {
            AndPredicate<?> andPredicate = (AndPredicate) predicate;
            if(andPredicate.predicates().isEmpty()) return empty();
            return Expressions.join(toExpressions(andPredicate.predicates()), "(", " and ", ")");
        }
        if (predicate instanceof OrPredicate) {
            OrPredicate<?> orPredicate = (OrPredicate) predicate;
            if(orPredicate.predicates().isEmpty()) return empty();
			if(!orPredicate.predicates().safeCast(AllPredicate.class).isEmpty()) return empty();
            return Expressions.join(toExpressions(orPredicate.predicates()), "(", " or ", ")");
        }
        if (predicate instanceof NullPredicate) {
            return textOnly("is null");
        }
        if (predicate instanceof NotNullPredicate) {
            return textOnly("is not null");
        }
        if (predicate instanceof EqualsPredicate) {
            return expression("= ?", getValue(predicate));
        }
        if (predicate instanceof NotEqualsPredicate) {
            return expression("!= ?", getValue(predicate));
        }
        if (predicate instanceof Not) {
            return textOnly("not").join(toSql(((Not) predicate).predicate()));
        }
        if (predicate instanceof GreaterThan) {
            return expression("> ?", getValue(predicate));
        }
        if (predicate instanceof GreaterThanOrEqualTo) {
            return expression(">= ?", getValue(predicate));
        }
        if (predicate instanceof LessThan) {
            return expression("< ?", getValue(predicate));
        }
        if (predicate instanceof LessThanOrEqualTo) {
            return expression("<= ?", getValue(predicate));
        }
        if (predicate instanceof Between) {
            Between between = (Between) predicate;
            return expression("between ? and ?", sequence(between.lower(), between.upper()));
        }
        if (predicate instanceof InPredicate) {
            InPredicate<Object> inPredicate = Unchecked.cast(predicate);
            Sequence<Object> sequence = sequence(inPredicate.values());
            if (sequence instanceof Expressible) {
                Expression pair = ((Expressible) sequence).express();
                return expression("in ( " + pair.text() + ")", pair.parameters());
            }
            return expression(repeat("?").take(sequence.size()).toString("in (", ",", ")"), sequence);
        }
        if (predicate instanceof StartsWithPredicate) {
            return expression("like ?", sequence((Object) (((StartsWithPredicate) predicate).value() + "%")));
        }
        if (predicate instanceof EndsWithPredicate) {
            return expression("like ?", sequence((Object) ("%" + ((EndsWithPredicate) predicate).value())));
        }
        if (predicate instanceof ContainsPredicate) {
            return expression("like ?", sequence((Object) ("%" + ((ContainsPredicate) predicate).value() + "%")));
        }
        throw new UnsupportedOperationException("Unsupported predicate " + predicate);
    }

    private static Sequence<Expression> toExpressions(Sequence<? extends Predicate<?>> predicates) {
        return predicates.map(toSql());
    }

    private static Sequence<Object> getValue(Predicate<?> predicate) {
        return sequence(((Value) predicate).value());
    }

    public static Function1<Predicate<?>, Expression> toSql() {
        return new Function1<Predicate<?>, Expression>() {
            public Expression call(Predicate<?> predicate) throws Exception {
                return toSql(predicate);
            }
        };
    }

}
