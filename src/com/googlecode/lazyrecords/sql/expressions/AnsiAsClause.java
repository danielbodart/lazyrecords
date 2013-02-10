package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Metadata;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.expression;
import static com.googlecode.lazyrecords.sql.expressions.TextOnlyExpression.textOnly;
import static com.googlecode.totallylazy.Option.none;

public class AnsiAsClause extends CompoundExpression implements AsClause {
    private final Option<Expression> as;
    private String alias;

    private AnsiAsClause(Option<Expression> as, String alias) {
        super(expression(as), textOnly(alias));
        this.as = as;
        this.alias = alias;
    }

    public static AsClause asClause(Option<Expression> as, String alias) {
        return new AnsiAsClause(as, alias);
    }

    public static AsClause asClause(String alias) {
        return asClause(none(Expression.class), alias);
    }

    public static Option<AsClause> asClause(Metadata<?> metadata) {
        return metadata.metadata(Keywords.alias).map(functions.asClause);
    }

    @Override
    public Option<Expression> as() {
        return as;
    }

    @Override
    public String alias() {
        return alias;
    }

    public static class functions {
        public static Mapper<String, AsClause> asClause = new Mapper<String, AsClause>() {
            @Override
            public AsClause call(String s) throws Exception {
                return asClause(s);
            }
        };
    }
}
