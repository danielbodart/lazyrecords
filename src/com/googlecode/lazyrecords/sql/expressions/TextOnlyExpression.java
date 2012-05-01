package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

public class TextOnlyExpression extends AbstractExpression {
    private final String text;

    protected TextOnlyExpression(String text) {
        this.text = text;
    }

    public static TextOnlyExpression textOnly(Object expression) {
        return new TextOnlyExpression(expression.toString());
    }

    public String text() {
        return text;
    }

    public Sequence<Object> parameters() {
        return Sequences.empty();
    }
}
