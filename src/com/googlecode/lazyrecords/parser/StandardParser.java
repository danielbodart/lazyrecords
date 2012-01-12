package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyparsec.error.ParserException;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;


public class StandardParser implements PredicateParser {
    public Predicate<Record> parse(String raw, Sequence<? extends Keyword> implicits) throws IllegalArgumentException{
        try {
            final String query = raw.trim();
            if (Strings.isEmpty(query)) {
                return Predicates.all();
            }
            return Grammar.PARSER(implicits.safeCast(Keyword.class)).parse(query);
        } catch (ParserException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
