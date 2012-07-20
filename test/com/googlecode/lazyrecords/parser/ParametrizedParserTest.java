package com.googlecode.lazyrecords.parser;

import com.googlecode.totallylazy.Predicate;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Dates;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ParametrizedParserTest {
    @Test
    public void canInjectCurrentTime() throws Exception{
        PredicateParser parser = new ParametrizedParser(new StandardParser(), new ParserParameters().add("now", Dates.date(2001, 2, 3)));
        Keyword<Date> created = keyword("Created", Date.class);

        Predicate<Record> predicate = parser.parse("Created > $now$", sequence(created));

        assertThat(predicate.matches(Record.constructors.record().set(created, date(2001, 2, 4))), is(true));
        assertThat(predicate.matches(Record.constructors.record().set(created, date(2001, 2, 3))), is(false));
        assertThat(predicate.matches(Record.constructors.record().set(created, date(2001, 2, 2))), is(false));

    }
}
