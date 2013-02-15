package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyparsec.error.ParserException;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.DateMapping;
import com.googlecode.lazyrecords.mappings.IntegerMapping;
import com.googlecode.lazyrecords.mappings.LongMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.time.DateConverter;
import com.googlecode.totallylazy.time.DateFormatConverter;
import com.googlecode.totallylazy.time.Dates;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.googlecode.totallylazy.time.DateFormatConverter.defaultConverter;


public class StandardParser implements PredicateParser {
    private final StringMappings mappings;

    public StandardParser() {
        this(new ParserDateConverter());
    }

    public StandardParser(ParserDateConverter dateConverter) {
        this(new StringMappings().
                add(Integer.class, new IntegerMapping()).
                add(Long.class, new LongMapping()).
                add(Date.class, new DateMapping(dateConverter)));
    }

    public StandardParser(StringMappings mappings) {
        this.mappings = mappings;
    }

    public Predicate<Record> parse(String raw, Sequence<? extends Keyword<?>> implicits) throws IllegalArgumentException{
        try {
            final String query = raw.trim();
            if (Strings.isEmpty(query)) {
                return Predicates.all();
            }
            return Grammar.PARSER(implicits, mappings).parse(query);
        } catch (ParserException e) {
            throw new IllegalArgumentException(raw, e);
        }
    }
}
