package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;

import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;

public interface Keyword<T> extends Named, Metadata<Keyword<T>>, Callable1<Record, T>, GenericType<T>, Comparable<Keyword<T>> {
    Record metadata();

    class constructors {
        public static ImmutableKeyword<Object> keyword(String value) {
            return new ImmutableKeyword<Object>(value, Object.class);
        }

        public static <T> ImmutableKeyword<T> keyword(String value, Class<? extends T> aClass) {
            return new ImmutableKeyword<T>(value, aClass);
        }

        public static <T> ImmutableKeyword<T> keyword(Keyword<? extends T> keyword) {
            return keyword(keyword.name(), keyword.forClass()).metadata(keyword.metadata());
        }

        public static <T> CompositeKeyword<T> compose(Callable2<? super T, ? super T, ? extends T> combiner, Sequence<? extends Keyword<T>> keywords) {
            return CompositeKeyword.compose(combiner, keywords);
        }
    }

    class methods {
        public static Keyword<Object> matchKeyword(String name, Sequence<? extends Keyword<?>> definitions) {
            return matchKeyword(name, definitions, Keyword.functions.name());
        }

        public static Keyword<Object> matchKeyword(String shortName, Sequence<? extends Keyword<?>> definitions, Function1<Keyword<?>, String> extractor) {
            return definitions.<Keyword<Object>>unsafeCast().find(where(extractor, equalIgnoringCase(shortName))).getOrElse(Keyword.constructors.keyword(shortName));
        }

        public static boolean equalTo(Keyword<?> keyword, Keyword<?> other) {
            return keyword.name().equalsIgnoreCase(other.name());
        }

        public static Sequence<Keyword<?>> keywords(Sequence<Record> results) {
            return results.flatMap(Record.functions.keywords).unique().realise();
        }
    }

    class functions {
        public static Function1<Keyword<?>, String> name = new Function1<Keyword<?>, String>() {
            @Override
            public String call(Keyword<?> keyword) throws Exception {
                return keyword.name();
            }
        };

        public static Function1<Keyword<?>, String> name() { return name; }

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

        public static <T, M> Function1<Keyword<T>, Keyword<T>> setMetadata(final Keyword<M> name, final M value) {
            return new Function1<Keyword<T>, Keyword<T>>() {
                public Keyword<T> call(Keyword<T> keyword) throws Exception {
                    return keyword.metadata(name, value);
                }
            };
        }
    }
}