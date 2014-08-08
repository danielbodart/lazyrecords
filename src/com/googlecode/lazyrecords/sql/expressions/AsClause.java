package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;

public interface AsClause extends Expression {
    TextOnlyExpression as = Expressions.textOnly("as");

    Option<Expression> as();
    String alias();

    public static class functions {
        public static Mapper<AsClause, String> alias() {
            return new Mapper<AsClause, String>() {
                @Override
                public String call(AsClause asClause) throws Exception {
                    return asClause.alias();
                }
            };
        }
    }
}
