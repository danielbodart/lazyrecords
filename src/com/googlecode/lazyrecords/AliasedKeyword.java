package com.googlecode.lazyrecords;

public class AliasedKeyword<T> extends AbstractKeyword<T>{
    private final Keyword<T> source;
    private final String name;

    public AliasedKeyword(Keyword<T> source, String name) {
        this.source = source;
        this.name = name;
    }

    public Keyword<T> source() {
        return source;
    }

    public String name() {
        return name;
    }

    public Class<T> forClass() {
        return source.forClass();
    }

    @Override
    public Record metadata() {
        return source.metadata();
    }

    @Override
    public Keyword<T> metadata(Record metadata) {
        source.metadata(metadata);
        return this;
    }

    @Override
    public T call(Record record) throws Exception {
        return record.get(source);
    }
}
