package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.callables.JoinString;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.expressions.SetFunctionType.setFunctionType;

public class SelectList extends CompoundExpression {
    public SelectList(Sequence<Keyword<?>> select) {
        super(select.map(derivedColumn()));
    }

    public static SelectList selectList(final Sequence<Keyword<?>> select) {
        return new SelectList(select);
    }

    @Override
    public String text() {
        return expressions.map(Expressions.text()).toString();
    }

    public static Function1<Keyword<?>, Expression> derivedColumn() {
        return new Function1<Keyword<?>, Expression>() {
            public Expression call(Keyword<?> keyword) throws Exception {
                return derivedColumnWithAlias(keyword);
            }
        };
    }

    public static <T> AbstractExpression derivedColumn(Callable1<? super Record, T> callable) {
        if (callable instanceof CompositeKeyword) {
            CompositeKeyword<?> composite = (CompositeKeyword<?>) callable;
            return Expressions.join(composite.keywords().map(name()), "(", combiner(composite.combiner()), ")");
        }
        if (callable instanceof Aggregate) {
            Aggregate aggregate = (Aggregate) callable;
            return setFunctionType(aggregate.reducer(), aggregate.source());
        }
        if (callable instanceof AliasedKeyword) {
            AliasedKeyword aliasedKeyword = (AliasedKeyword) callable;
            return name(aliasedKeyword.source());
        }
        if (callable instanceof Keyword) {
            Keyword<?> keyword = (Keyword) callable;
            return name(keyword);
        }
        if (callable instanceof SelectCallable) {
            Sequence<Keyword<?>> keywords = ((SelectCallable) callable).keywords();
            return selectList(keywords);
        }
        throw new UnsupportedOperationException("Unsupported reducer " + callable);
    }

    public static <T> AbstractExpression derivedColumnWithAlias(Callable1<? super Record, T> callable) {
        AbstractExpression expression = derivedColumn(callable);
        if (callable instanceof Aliased) {
            return expression.join(asClause((Keyword<?>) callable));
        }
        return expression;
    }

    private static String combiner(Callable2<?, ?, ?> combiner) {
        if (combiner instanceof JoinString) {
            return "||";
        }
        throw new UnsupportedOperationException("Unsupported combiner " + combiner);
    }

    public static boolean isLongName(Keyword<?> keyword) {
        return keyword.name().contains(".");
    }

    public static Expression asClause(Keyword<?> keyword) {
        return asClause(keyword.name());
    }

    public static AbstractExpression asClause(String name) {
        return textOnly("as " + name);
    }
}
