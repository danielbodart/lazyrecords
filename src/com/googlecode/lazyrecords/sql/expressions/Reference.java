package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.quote;
import static java.lang.String.format;

public abstract class Reference<Self extends Reference<Self>> extends AbstractExpression {
    protected final String qualifier;
    protected final String name;
    protected final String alias;

    protected Reference(String qualifier, String name, String alias) {
        this.qualifier = quote(qualifier);
        this.name = quote(name);
        this.alias = quote(alias);
    }

    protected abstract Self self(String qualifier, String text, String alias);

    public Self qualify(String qualifier) {
        if (isQualified()) return self(this.qualifier, name, alias);
        return self(qualifier, name, alias);
    }

    public Self alias(String alias) {
        if (isAliased()) return self(qualifier, name, this.alias);
        return self(qualifier, name, alias);
    }

    public boolean isQualified() {
        return !qualifier.isEmpty();
    }

    public boolean isAliased() {
        return !alias.isEmpty();
    }

    public String name() {
        return isAliased() ? alias : name;
    }

    @Override
    public String text() {
        return isQualified() ? format("%s.%s", qualifier, aliased()) : aliased();
    }

    private String aliased() {
        return isAliased() ? format("%s %s", name, alias) : name;
    }

    @Override
    public Sequence<Object> parameters() {
        return Sequences.empty();
    }
}
