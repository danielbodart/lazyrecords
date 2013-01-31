package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Aggregate;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.CompositeKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Binary;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.callables.JoinString;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.lazyrecords.sql.expressions.SetFunctionType.setFunctionType;

public class AnsiValueExpression {
    public static ValueExpression valueExpression(Callable1<? super Record, ?> callable) {
        if (callable instanceof CompositeKeyword) {
            CompositeKeyword<?> composite = (CompositeKeyword<?>) callable;
            Binary<?> combiner = composite.combiner();
            if (combiner instanceof JoinString) {
                return concat(composite.keywords());
            }
            throw new UnsupportedOperationException("Unsupported combiner " + combiner);
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
        throw new UnsupportedOperationException("Unsupported reducer " + callable);
    }

    public static ValueExpression concat(Sequence<? extends Keyword<?>> keywords) {
        return new CompositeExpression(keywords.map(name()), "(", "||", ")");
    }
}
