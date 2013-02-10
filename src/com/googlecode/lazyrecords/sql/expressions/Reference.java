package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.*;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.quote;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Strings.empty;
import static java.lang.String.format;

public abstract class Reference<Self extends Reference<Self>> extends AbstractExpression {
    protected final String name;
    public final Option<String> qualifier;

    protected Reference(String name, Option<String> qualifier) {
        this.qualifier = qualifier.map(quote);
        this.name = quote(name);
    }

    protected abstract Self self(String text, Option<String> qualifier);

    public Option<String> qualifier() {
        return qualifier;
    }

    public Self qualify(String qualifier) {
        if (isQualified()) return self(name, this.qualifier);
        return self(name, some(qualifier));
    }

    public boolean isQualified() {
        return !qualifier.filter(not(empty)).isEmpty();
    }

    public String name() {
        return name;
    }

    @Override
    public String text() {
        return isQualified() ? format("%s.%s", qualifier.get(), name()) : name();
    }

    @Override
    public Sequence<Object> parameters() {
        return Sequences.empty();
    }
}
