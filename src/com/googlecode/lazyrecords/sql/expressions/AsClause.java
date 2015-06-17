package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;

public interface AsClause extends Expression {
    TextOnlyExpression as = Expressions.textOnly("as");

    Option<Expression> as();
    String alias();

    public static class functions {
        public static Function1<AsClause, String> alias() {
            return new Function1<AsClause, String>() {
                @Override
                public String call(AsClause asClause) throws Exception {
                    return asClause.alias();
                }
            };
        }
    }
}
