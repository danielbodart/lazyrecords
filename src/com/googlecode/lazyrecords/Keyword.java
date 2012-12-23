package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.GenericType;

public interface Keyword<T> extends Named, Callable1<Record, T>, GenericType<T>, Comparable<Keyword<T>> {
    Record metadata();
    Keyword<T> metadata(Record metadata);
    <M> Keyword<T> setMetadata(Keyword<M> name, M value);

	public static class functions {
		public static Function1<Keyword<?>, String> name() {
			return new Function1<Keyword<?>, String>() {
				@Override
				public String call(Keyword<?> keyword) throws Exception {
					return keyword.name();
				}
			};
		}

		public static <T> Function1<Keyword<?>, T> metadata(final Keyword<T> metadataKey) {
			return new Function1<Keyword<?>, T>() {
				public T call(Keyword<?> keyword) throws Exception {
					return keyword.metadata().get(metadataKey);
				}
			};
		}
		public static <T> Function1<Keyword<T>, Keyword<T>> metadata(final Record metadata) {
			return new Function1<Keyword<T>, Keyword<T>>() {
				public Keyword<T> call(Keyword<T> keyword) throws Exception {
					return keyword.metadata(metadata);
				}
			};
		}
		public static <T,M> Function1<Keyword<T>, Keyword<T>> setMetadata(final Keyword<M> name, final M value) {
			return new Function1<Keyword<T>, Keyword<T>>() {
				public Keyword<T> call(Keyword<T> keyword) throws Exception {
					return keyword.setMetadata(name, value);
				}
			};
		}
	}
}