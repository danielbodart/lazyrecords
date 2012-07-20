package com.googlecode.lazyrecords;

public abstract class AbstractKeyword<T> implements Keyword<T> {
    private Record metadata = Record.constructors.record();

    @Override
    public boolean equals(Object other) {
        return other instanceof Keyword && Keywords.equalto(this, (Keyword) other);
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

    public Record metadata() {
        return metadata;
    }

    public Keyword<T> metadata(Record metadata) {
        this.metadata  = metadata;
        return this;
    }

    @Override
    public int compareTo(Keyword<T> keyword) {
        return name().compareTo(keyword.name());
    }
}
