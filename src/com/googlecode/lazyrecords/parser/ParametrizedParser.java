package com.googlecode.lazyrecords.parser;

import com.googlecode.funclate.CompositeFunclate;
import com.googlecode.funclate.Funclate;
import com.googlecode.funclate.StringFunclate;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.DateConverter;
import com.googlecode.totallylazy.time.Dates;

import java.util.Date;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.DateConverter.functions.format;

public class ParametrizedParser implements PredicateParser {
    private final PredicateParser parser;
    private final ParserDateConverter dateConverter;
    private final ParserFunctions parserFunctions;
    private final ParserParameters data;

    public ParametrizedParser(PredicateParser parser, ParserDateConverter dateConverter, ParserParameters data) {
        this(parser, dateConverter, new ParserFunctions(), data);
    }

    public ParametrizedParser(PredicateParser parser, ParserDateConverter dateConverter, ParserFunctions parserFunctions, ParserParameters data) {
        this.parser = parser;
        this.dateConverter = dateConverter;
        this.parserFunctions = parserFunctions;
        this.data = data;
    }

    public Predicate<Record> parse(String query, Sequence<? extends Keyword<?>> implicits) throws IllegalArgumentException {
        try {
            Funclate funclate = new StringFunclate(query);
            funclate.add(instanceOf(Date.class), format(dateConverter));

            sequence(parserFunctions.functions()).fold(funclate, CompositeFunclate.functions.addCallable());

            String newQuery = funclate.render(data.values());
            return parser.parse(newQuery, implicits);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
