package com.googlecode.lazyrecords.parser;

import com.googlecode.funclate.CompositeFunclate;
import com.googlecode.funclate.Funclate;
import com.googlecode.funclate.StringFunclate;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Dates;

import java.util.Date;

import static com.googlecode.totallylazy.Predicates.instanceOf;
import static com.googlecode.totallylazy.Sequences.sequence;

public class ParametrizedParser implements PredicateParser {
    private final PredicateParser parser;
    private final ParserFunctions parserFunctions;
    private final ParserParameters data;

    public ParametrizedParser(PredicateParser parser, ParserParameters data) {
        this(parser, new ParserFunctions(), data);
    }

    public ParametrizedParser(PredicateParser parser, ParserFunctions parserFunctions, ParserParameters data) {
        this.parser = parser;
        this.parserFunctions = parserFunctions;
        this.data = data;
    }

    public Predicate<Record> parse(String query, Sequence<? extends Keyword<?>> implicits) throws IllegalArgumentException {
        try {
            Funclate funclate = new StringFunclate(query);
            funclate.add(instanceOf(Date.class), formatDate());

            sequence(parserFunctions.functions()).fold(funclate, CompositeFunclate.functions.addCallable());

            String newQuery = funclate.render(data.values());
            return parser.parse(newQuery, implicits);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Callable1<Date, String> formatDate() {
        return new Callable1<Date, String>() {
            public String call(Date date) throws Exception {
                return Dates.RFC3339withMilliseconds().format(date); // this format isn't lossy
            }
        };
    }
}
