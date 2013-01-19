package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;

public class Keywords {
    public static final Keyword<Boolean> UNIQUE = Keyword.constructors.keyword("unique", Boolean.class);
    public static final Keyword<Boolean> INDEXED = Keyword.constructors.keyword("indexed", Boolean.class);
    public static final Keyword<Definition> definition = Keyword.constructors.keyword("definition", Definition.class);

    public static Keyword<Object> matchKeyword(String name, Sequence<? extends Keyword<?>> definitions) {
        return matchKeyword(name, definitions, Keyword.functions.name());
    }

    public static Keyword<Object> matchKeyword(String shortName, Sequence<? extends Keyword<?>> definitions, Function1<Keyword<?>, String> extractor) {
        return definitions.<Keyword<Object>>unsafeCast().find(where(extractor, equalIgnoringCase(shortName))).getOrElse(Keyword.constructors.keyword(shortName));
    }

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

    /** @deprecated Replaced by {@link Keyword.constructors#keyword(Keyword)}  } */
    @Deprecated
    public static <T> ImmutableKeyword<T> keyword(Keyword<? extends T> keyword) {
        return Keyword.constructors.keyword(keyword);
    }

    public static boolean equalto(Keyword<?> keyword, Keyword<?> other) {
        return keyword.name().equalsIgnoreCase(other.name());
    }

    public static Sequence<Keyword<?>> keywords(Sequence<Record> results) {
        return results.flatMap(Record.functions.keywords()).unique().realise();
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
