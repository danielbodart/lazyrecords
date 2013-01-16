package com.googlecode.lazyrecords;

public class AliasedKeyword<T> extends AbstractKeyword<T> implements Aliased {
    private final Keyword<T> source;
    private final String name;

    public AliasedKeyword(Keyword<T> source, String name) {
        this(source, name, source.metadata());
    }

    public AliasedKeyword(Keyword<T> source, String name, Record metadata) {
        super(metadata);
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
        return super.metadata();
    }

    @Override
    public AliasedKeyword<T> metadata(Record metadata) {
        return new AliasedKeyword<T>(source, name, metadata);
    }

    @Override
    public T call(Record record) throws Exception {
        return record.get(source);
    }
}
