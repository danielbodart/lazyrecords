package com.googlecode.lazyrecords.parser;

import com.googlecode.funclate.StringFunclate;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.time.Dates;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Callables.returnArgument;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParametrizedParserTest {
    @Test
    public void canInjectCurrentTime() throws Exception {
        PredicateParser parser = new ParametrizedParser(new StandardParser(), new ParserParameters().add("now", Dates.date(2001, 2, 3)));
        Keyword<Date> created = keyword("Created", Date.class);

        Predicate<Record> predicate = parser.parse("Created > \"$now$\"", sequence(created));

        assertThat(predicate.matches(Record.constructors.record().set(created, date(2001, 2, 4))), is(true));
        assertThat(predicate.matches(Record.constructors.record().set(created, date(2001, 2, 3))), is(false));
        assertThat(predicate.matches(Record.constructors.record().set(created, date(2001, 2, 2))), is(false));

    }

    @Test
    public void canInjectFunctions() {
        ParserFunctions parserFunctions = new ParserFunctions().
                add("name", Predicates.always(), StringFunclate.functions.first(returnArgument(String.class).then(Strings.reverse()))).
                add("shoeSize", Predicates.always(), StringFunclate.functions.first(returnArgument(String.class).then(Strings.reverse())));

        PredicateParser parser = new ParametrizedParser(new StandardParser(), parserFunctions, new ParserParameters());

        Keyword<String> data = keyword("data", String.class);
        Keyword<String> shoeSize = keyword("shoeSize", String.class);
        Predicate<Record> predicate = parser.parse("data:$name(\"trautS\")$ shoeSize:\"$shoeSize(\"5.01\")$\"", sequence(data, shoeSize));

        assertThat(predicate.matches(record(data, "Stuart", shoeSize, "10.5")), is(true));
        assertThat(predicate.matches(record(data, "Stuart", shoeSize, "11")), is(false));
        assertThat(predicate.matches(record(data, "Phill", shoeSize, "10.5")), is(false));
        assertThat(predicate.matches(record(data, "Stuart", shoeSize, "11")), is(false));
    }

}
