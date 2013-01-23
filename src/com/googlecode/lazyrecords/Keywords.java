package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

public class Keywords {
    public static final Keyword<Boolean> UNIQUE = Keyword.constructors.keyword("unique", Boolean.class);
    public static final Keyword<Boolean> INDEXED = Keyword.constructors.keyword("indexed", Boolean.class);
    public static final Keyword<String> qualifier = Keyword.constructors.keyword("qualifier", String.class);
    public static final Keyword<String> alias = Keyword.constructors.keyword("alias", String.class);

    /** @deprecated Replaced by {@link Keyword.constructors#keyword(String)}  } */
    @Deprecated
    public static ImmutableKeyword<Object> keyword(String value) {
        return Keyword.constructors.keyword(value);
    }

    /** @deprecated Replaced by {@link Keyword.constructors#keyword(String, Class)}  } */
    @Deprecated
    public static <T> ImmutableKeyword<T> keyword(String value, Class<? extends T> aClass) {
        return Keyword.constructors.keyword(value, aClass);
    }

    /** @deprecated Replaced by {@link Keyword.methods#matchKeyword(String, Sequence)}  } */
    @Deprecated
    public static Keyword<Object> matchKeyword(String name, Sequence<? extends Keyword<?>> definitions) {
        return Keyword.methods.matchKeyword(name, definitions);
    }

    /** @deprecated Replaced by {@link Keyword.methods#matchKeyword(String, Sequence, Function1)}  } */
    @Deprecated
    public static Keyword<Object> matchKeyword(String shortName, Sequence<? extends Keyword<?>> definitions, Function1<Keyword<?>, String> extractor) {
        return Keyword.methods.matchKeyword(shortName, definitions, extractor);
    }

    /** @deprecated Replaced by {@link Keyword.methods#equalTo(Keyword, Keyword)}  } */
    @Deprecated
    public static boolean equalto(Keyword<?> keyword, Keyword<?> other) {
        return Keyword.methods.equalTo(keyword, other);
    }

    /** @deprecated Replaced by {@link Keyword.methods#keywords(Sequence)}  } */
    @Deprecated
    public static Sequence<Keyword<?>> keywords(Sequence<Record> results) {
        return Keyword.methods.keywords(results);
    }

    /** @deprecated Replaced by {@link Keyword.functions#name } */
    @Deprecated
    public static Function1<Keyword<?>, String> name() {
        return Keyword.functions.name;
    }

    /** @deprecated Replaced by {@link Keyword.functions#metadata(Keyword)}  } */
    @Deprecated
    public static <T> Function1<Keyword<?>, T> metadata(final Keyword<T> metadataKey) {
        return Keyword.functions.metadata(metadataKey);
    }

    /** @deprecated Replaced by {@link Record.functions#keywords} */
    @Deprecated
    public static Function1<Record, Sequence<Keyword<?>>> keywords() {
        return Record.functions.keywords;
    }
}
