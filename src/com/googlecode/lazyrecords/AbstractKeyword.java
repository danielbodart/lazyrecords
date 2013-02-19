package com.googlecode.lazyrecords;

import static java.lang.String.format;

public abstract class AbstractKeyword<T> extends AbstractMetadata<Keyword<T>> implements Keyword<T> {
    protected AbstractKeyword(Record metadata) {
        super(metadata);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Keyword && Keyword.methods.equalTo(this, (Keyword) other);
    }

    @Override
    public int hashCode() {
        return name().toLowerCase().hashCode();
    }

    public T call(Record record) throws Exception {
        return record.get(this);
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public int compareTo(Keyword<T> keyword) {
        return name().toLowerCase().compareTo(keyword.name().toLowerCase());
    }
}
