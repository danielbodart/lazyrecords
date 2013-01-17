package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;

public class Keywords {
    public static final Keyword<Boolean> UNIQUE = Keywords.keyword("unique", Boolean.class);
    public static final Keyword<Boolean> INDEXED = Keywords.keyword("indexed", Boolean.class);
    public static final Keyword<String> DEFINED = Keywords.keyword("defined", String.class);

	public static Keyword<Object> matchKeyword(String name, Sequence<? extends Keyword<?>> definitions) {
        return matchKeyword(name, definitions, Keyword.functions.name());
    }

    public static Keyword<Object> matchKeyword(String shortName, Sequence<? extends Keyword<?>> definitions, Function1<Keyword<?>, String> extractor) {
        return definitions.<Keyword<Object>>unsafeCast().find(where(extractor, equalIgnoringCase(shortName))).getOrElse(Keywords.keyword(shortName));
    }

    public static ImmutableKeyword<Object> keyword(String value) {
        return new ImmutableKeyword<Object>(value, Object.class);
    }

    public static <T> ImmutableKeyword<T> keyword(String value, Class<? extends T> aClass) {
        return new ImmutableKeyword<T>(value, aClass);
    }

    public static <T> ImmutableKeyword<T> keyword(Keyword<? extends T> keyword) {
        return keyword(keyword.name(), keyword.forClass()).metadata(keyword.metadata());
    }

    public static boolean equalto(Keyword<?> keyword, Keyword<?> other) {
        return keyword.name().equalsIgnoreCase(other.name());
    }

    public static Sequence<Keyword<?>> keywords(Sequence<Record> results) {
        return results.flatMap(Record.functions.keywords()).unique().realise();
    }

    /**
	 * Moved to Keyword.functions.name()
	 */
	@Deprecated
	public static Function1<Keyword<?>, String> name() {
		return Keyword.functions.name();
	}

	/**
	 * Moved to Keyword.functions.metadata()
	 */
	@Deprecated
	public static <T> Function1<Keyword<?>, T> metadata(final Keyword<T> metadataKey) {
		return Keyword.functions.metadata(metadataKey);
	}

	/**
	 * Moved to Record.functions.keywords()
	 */
	@Deprecated
	public static Function1<Record, Sequence<Keyword<?>>> keywords() {
		return Record.functions.keywords();
	}
}
