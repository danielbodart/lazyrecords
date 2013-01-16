package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.totallylazy.Callables.returnArgument;
import static com.googlecode.totallylazy.Sequences.sequence;

public abstract class AbstractExpression implements Expression {
    public CompoundExpression join(final Expression other) {
        return new CompoundExpression(this, other);
    }

    public CompoundExpression join(final Sequence<? extends Expression> expressions) {
        return new CompoundExpression(Sequences.<Expression>one(this).join(expressions));
    }

    public CompoundExpression join(final Sequence<? extends Expression> expressions, final String start, final String separator, final String end) {
        return new CompoundExpression(Sequences.<Expression>one(this).join(expressions), start, separator, end);
    }

    public String toString() {
        return toString(returnArgument());
    }

    public String toString(Callable1<Object, Object> valueConverter) {
        return Expressions.toString(this, valueConverter);
    }
}
