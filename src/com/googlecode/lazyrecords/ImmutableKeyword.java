package com.googlecode.lazyrecords;

import static com.googlecode.totallylazy.Unchecked.cast;

public class ImmutableKeyword<T> extends AbstractKeyword<T> {
    private final String name;
    private final Class<T> aClass;

    public ImmutableKeyword(String name, Class<? extends T> aClass) {
        if(name == null){
            throw new IllegalArgumentException("name");
        }
        this.name = name;
        this.aClass = cast(aClass);
    }


    public AliasedKeyword<T> as(String name) {
        return new AliasedKeyword<T>(this, name);
    }

    public AliasedKeyword<T> as(Keyword<T> keyword) {
        return new AliasedKeyword<T>(this, keyword.name());
    }

    public String name() {
        return name;
    }

    public Class<T> forClass() {
        return aClass;
    }

    @Override
    public ImmutableKeyword<T> metadata(Record metadata) {
        super.metadata(metadata);
        return this;
    }
}
