package com.googlecode.lazyrecords;

public abstract class AbstractKeyword<T> extends AbstractMetadata<Keyword<T>> implements Keyword<T> {
	private final String name;
	private final int hashCode;

	protected AbstractKeyword(Record metadata, String name) {
        super(metadata);
		this.name = name;
		this.hashCode = name().toLowerCase().hashCode();
	}

    @Override
    public String name(){
        return name;
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof Keyword && Keyword.methods.equalTo(this, (Keyword) other);
    }

    @Override
    public int hashCode() {
        return hashCode;
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
