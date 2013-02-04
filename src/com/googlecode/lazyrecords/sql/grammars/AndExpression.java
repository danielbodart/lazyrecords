package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.sql.expressions.CompoundExpression;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;

public class AndExpression extends CompoundExpression {
    private AndExpression(final Sequence<? extends Expression> expressions) {
        super(expressions, "(", " and ", ")");
    }

    public static AndExpression andExpression(final Sequence<? extends Expression> expressions) {
        return new AndExpression(expressions);
    }

    public static AndExpression andExpression(final Expression... expressions) {
        return new AndExpression(sequence(expressions));
    }
}
