package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.sql.expressions.CompoundExpression;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.totallylazy.Sequence;

public class OrExpression extends CompoundExpression {
    private OrExpression(final Sequence<? extends Expression> expressions) {
        super(expressions, "(", " or ", ")");
    }

    public static OrExpression orExpression(final Sequence<? extends Expression> expressions) {
        return new OrExpression(expressions);
    }
}
