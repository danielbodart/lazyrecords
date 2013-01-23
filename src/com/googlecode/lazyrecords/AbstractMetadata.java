package com.googlecode.lazyrecords;

public abstract class AbstractMetadata<Self extends Metadata<Self>> implements Metadata<Self> {
    protected final Record metadata;

    protected AbstractMetadata(Record metadata){
        this.metadata = metadata;
    }
    @Override
    public Record metadata() {
        return metadata;
    }

    @Override
    public <T> T metadata(Keyword<T> keyword) {
        return metadata.get(keyword);
    }

    @Override
    public <T> Self metadata(Keyword<T> keyword, T value) {
        return metadata(metadata.set(keyword, value));
    }
}
