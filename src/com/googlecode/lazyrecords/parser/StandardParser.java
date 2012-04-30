package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyparsec.error.ParserException;
import com.googlecode.lazyrecords.mappings.IntegerMapping;
import com.googlecode.lazyrecords.mappings.LongMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;


public class StandardParser implements PredicateParser {
    private final StringMappings mappings;

    public StandardParser() {
        mappings = new StringMappings().
                add(Integer.class, new IntegerMapping()).
                add(Long.class, new LongMapping());
    }

    public Predicate<Record> parse(String raw, Sequence<? extends Keyword<?>> implicits) throws IllegalArgumentException{
        try {
            final String query = raw.trim();
            if (Strings.isEmpty(query)) {
                return Predicates.all();
            }
            return Grammar.PARSER(implicits, mappings).parse(query);
        } catch (ParserException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
