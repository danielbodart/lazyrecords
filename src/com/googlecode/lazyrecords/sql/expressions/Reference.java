package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.*;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.quote;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Strings.empty;
import static java.lang.String.format;

public abstract class Reference<Self extends Reference<Self>> extends AbstractExpression {
    protected final String name;
    protected final Option<String> qualifier;
    protected final Option<String> alias;

    protected Reference(String name, Option<String> qualifier, Option<String> alias) {
        this.qualifier = qualifier.map(quote);
        this.name = quote(name);
        this.alias = alias.map(quote);
    }

    protected abstract Self self(String text, Option<String> qualifier, Option<String> alias);

    public Self qualify(String qualifier) {
        if (isQualified()) return self(name, this.qualifier, alias);
        return self(name, some(qualifier), alias);
    }

    public Self alias(String alias) {
        if (isAliased()) return self(name, qualifier, this.alias);
        return self(name, qualifier, some(alias));
    }

    public boolean isQualified() {
        return !qualifier.filter(not(empty)).isEmpty();
    }

    public boolean isAliased() {
        return !alias.filter(not(empty)).isEmpty();
    }

    public String name() {
        return isAliased() ? alias.get() : name;
    }

    @Override
    public String text() {
        return isQualified() ? format("%s.%s", qualifier.get(), aliased()) : aliased();
    }

    private String aliased() {
        return isAliased() ? format("%s %s", name, alias.get()) : name;
    }

    @Override
    public Sequence<Object> parameters() {
        return Sequences.empty();
    }
}
