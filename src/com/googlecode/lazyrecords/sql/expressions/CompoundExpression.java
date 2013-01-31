package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;

public class CompoundExpression extends AbstractExpression {
    protected final Sequence<Expression> expressions;
    private final String start;
    private final String separator;
    private final String end;

    public CompoundExpression(final Expression... expressions) {
        this(sequence(expressions));
    }

    public CompoundExpression(final Sequence<? extends Expression> expressions) {
        this(expressions, "", " ", "");
    }

    public CompoundExpression(final Sequence<? extends Expression> expressions, String separator) {
        this(expressions, "", separator, "");
    }

    public CompoundExpression(final Sequence<? extends Expression> expressions, final String start, final String separator, final String end) {
        this.expressions = expressions.unsafeCast();
        this.start = start;
        this.separator = separator;
        this.end = end;
    }

    public String text() {
        return expressions.filter(not(instanceOf(EmptyExpression.class))).
                map(Expressions.text()).
                toString(start, separator, end).trim();
    }

    public Sequence<Object> parameters() {
        return expressions.flatMap(Expressions.parameters());
    }

    public Sequence<Expression> expressions() {
        return expressions;
    }

    public String start() {
        return start;
    }

    public String separator() {
        return separator;
    }

    public String end() {
        return end;
    }
}
