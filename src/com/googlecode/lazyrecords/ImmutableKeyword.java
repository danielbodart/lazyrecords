package com.googlecode.lazyrecords;

import static com.googlecode.totallylazy.Unchecked.cast;
import static java.lang.String.format;

public class ImmutableKeyword<T> extends AbstractKeyword<T> {
    private final String name;
    private final Class<T> aClass;

    public ImmutableKeyword(String name, Class<? extends T> aClass) {
        this(name, aClass, Record.constructors.record());
    }

    public ImmutableKeyword(String name, Class<? extends T> aClass, Record metadata) {
        super(metadata);
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

    public AliasedKeyword<T> of(Definition definition) {
        return setMetadata(Keywords.definition, definition).as(format("%s_%s", definition.name(), name));
    }

    public String name() {
        return name;
    }

    public Class<T> forClass() {
        return aClass;
    }

    @Override
    public ImmutableKeyword<T> metadata(Record metadata) {
        return new ImmutableKeyword<T>(name, aClass, metadata);
    }

    @Override
    public <M> ImmutableKeyword<T> setMetadata(Keyword<M> name, M value) {
        return metadata(metadata().set(name, value));
    }

}
