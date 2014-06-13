package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.Lucene;
import com.googlecode.lazyrecords.lucene.LuceneRecordsTest;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Strings;
import com.googlecode.yatspec.junit.Row;
import com.googlecode.yatspec.junit.SpecRunner;
import com.googlecode.yatspec.junit.Table;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static com.googlecode.lazyrecords.Grammar.record;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.time.Dates.date;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

@RunWith(SpecRunner.class)
public class StandardParserTest {
    private final StandardParser predicateParser = new StandardParser();
    private final Keyword<String> name = keyword("name", String.class);
    private final Keyword<String> age = keyword("age", String.class);
    private final Keyword<String> id = keyword("id", String.class);

    @Test
    public void producesTheSameResultAsLucene() throws Exception {
        QueryParser parser = new QueryParser(LuceneRecordsTest.VERSION, null, LuceneRecordsTest.ANALYZER);
        Predicate<Record> predicates = new StandardParser().parse("type:people OR (firstName:da* AND lastName:bod)", Sequences.<Keyword<?>>empty());
        Query query = new Lucene(new StringMappings()).query(predicates);
        Query plus = parser.parse("type:people (+firstName:da* +lastName:bod)");
        Query and = parser.parse("type:people OR (firstName:da* AND lastName:bod)");
        assertEquals(plus, and);
        assertEquals(query, and);
    }


    @Test
    public void shouldBeAbleToNestBrackets() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("id:\"X\" AND (name:\"dan\" AND age:\"old\" OR (name:\"bob\" AND age:\"young\"))", sequence(name, age));

        assertThat(predicate.matches(record(name, "bob", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "dan", name, "bob", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "stu")), is(false));
        assertThat(predicate.matches(record(id, "X", name, "dan", age, "old")), is(true));
        assertThat(predicate.matches(record(id, "X", name, "bob", age, "young")), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void shouldBeAbleToUseMoreThanOneBrackets() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("(name:\"dan\" AND age:\"old\") OR (name:\"bob\" AND age:\"old\")", sequence(name, age));

        assertThat(predicate.matches(record(name, "bob", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "bob", age, "old")), is(true));
        assertThat(predicate.matches(record(name, "dan", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "dan", age, "old")), is(true));
        assertThat(predicate.matches(record(name, "dan", name, "bob")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }


    @Test
    public void shouldBracketsAndNot() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:\"dan\" NOT (name:\"bob\" OR age:\"old\")", sequence(name, age));

        assertThat(predicate.matches(record(name, "bob", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "bob", age, "old")), is(false));
        assertThat(predicate.matches(record(name, "dan", age, "young")), is(true));
        assertThat(predicate.matches(record(name, "dan", age, "old")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsBracketingOR() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:\"dan\" OR (name:\"bob\" AND age:\"old\")", sequence(name, age));
        assertThat(predicate.matches(record(name, "bob", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "bob", age, "old")), is(true));
        assertThat(predicate.matches(record(name, "dan", age, "young")), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    private void assertLuceAndSqlSyntax(Predicate<Record> predicate) {
        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsBracketingAND() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:\"dan\" AND (name:\"bob\" OR age:\"old\")", sequence(name, age));
        assertThat(predicate.matches(record(name, "bob", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "bob", age, "old")), is(false));
        assertThat(predicate.matches(record(name, "dan", age, "young")), is(false));
        assertThat(predicate.matches(record(name, "dan", age, "old")), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBarfWithMissingClosingBracket() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:\"dan\" AND (name:\"bob\" OR age:\"old\"", sequence(name, age));

        predicate.matches(record(name, "bob", age, "young"));
    }

    @Test
    public void supportsImplicitKeywords() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("bob", sequence(name));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "dan")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsExplicitKeywords() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:bob", Sequences.<Keyword<?>>empty());

        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "dan")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsMultipleConditions() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:bob age:12", Sequences.<Keyword<?>>empty());

        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "13")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsMultipleConditionsSeparatedByManySpaces() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:bob    age:12", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "13")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsNegationWithImplicit() throws Exception {
        Predicate<Record> predicate = predicateParser.parse(" NOT bob age:12", sequence(keyword("name", String.class)));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "13")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsNegationWithExplicit() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("-name:bob age:12", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "13")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsOrWithImplicit() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("dan,bob", sequence(keyword("name", String.class)));
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceAndSqlSyntax(predicate);

    }

    @Test
    public void supportsAndWithExplicit() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:bodart AND title:baron", Sequences.<Keyword<?>>empty());
        Keyword<String> title = keyword("title", String.class);
        assertThat(predicate.matches(record().set(title, "baron").set(name, "bodart")), is(true));
        assertThat(predicate.matches(record().set(title, "duke").set(name, "bodart")), is(false));
        assertThat(predicate.matches(record().set(title, "baron").set(name, "greenback")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsOrWithExplicit() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:dan OR name:bob", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void explicitOrRequiresSpace() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("ORangutan ORangutan", sequence(name));
        assertThat(predicate.matches(record().set(name, "angutan")), is(false));
        assertThat(predicate.matches(record().set(name, "ORangutan")), is(true));
        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void ignoreWhitespaces() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("  name  :  dan  ,   bob  ", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsQuotedValue() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:\"Dan Bod\"", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bod")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsStartsWith() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:Dan*", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Bod")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsEndsWith() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:*Bod", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bod")), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsContains() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:*ell*", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Hello")), is(true));
        assertThat(predicate.matches(record().set(name, "Helo")), is(false));
        assertThat(predicate.matches(record().set(name, "ell")), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsQuotesContainingNonAlphaNumericCharacters() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("id:\"urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1\"", Sequences.<Keyword<?>>empty());


        assertThat(predicate.matches(record().set(id, "urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1")), is(true));
        assertThat(predicate.matches(record().set(id, "fail")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsQuotedName() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("\"First Name\":Dan", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("First Name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsEmptyQueries() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("First Name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));

        assertLuceneSyntax(predicate);
        // SQLRecords guards against ALLPredicate so does not create a where clause at all
    }

    @Test
    @Table ({
        @Row ({"2001/1/10"}),
        @Row ({"10/01/2001"}),
        @Row ({"10/1/2001"}),
        @Row ({"10/01/01"}),
        @Row ({"10/1/01"})
    })
    public void supportsExplicitDateBasedQueries(String query) throws Exception {
        Keyword<Date> dob = keyword("dob", Date.class);
        Predicate<Record> predicate = predicateParser.parse(format("dob:%s", query), Sequences.one(dob));

        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 10, 1))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10, 3, 15, 59, 123))), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    @Table ({
            @Row ({"2001/1/10"}),
            @Row ({"10/01/2001"}),
            @Row ({"10/1/2001"}),
            @Row ({"10/01/01"}),
            @Row ({"10/1/01"})
    })
    public void supportsImplicitDateBasedQueries(String query) throws Exception {
        Sequence<Keyword<?>> keywords = Sequences.<Keyword<?>>sequence(keyword("dob", Date.class));
        Predicate<Record> predicate = predicateParser.parse(query, keywords);

        assertThat(predicate.matches(record().set(keyword("dob", Date.class), date(2001, 1, 10))), is(true));
        assertThat(predicate.matches(record().set(keyword("dob", Date.class), date(2001, 10, 1))), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    @Table ({
            @Row({"2001/01/10 03:15:59"}),
            @Row ({"10/1/2001 03:15:59"}),
            @Row ({"10/01/01 03:15:59"})
    })
    public void supportsExplicitDateTimeBasedQueries(String query) throws Exception {
        Keyword<Date> dob = keyword("dob", Date.class);
        Predicate<Record> predicate = predicateParser.parse(format("dob:%s", query), Sequences.sequence(dob));

        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 10, 1))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10, 3, 15, 59, 123))), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    @Table ({
            @Row ({"10/1/2001 03:15:59"}),
            @Row ({"10/01/01 03:15:59"})
    })
    public void supportsImplicitDateTimeBasedQueries(String query) throws Exception {
        Sequence<Keyword<?>> keywords = Sequences.<Keyword<?>>sequence(keyword("dob", Date.class));
        Predicate<Record> predicate = predicateParser.parse(query, keywords);

        assertThat(predicate.matches(record().set(keyword("dob", Date.class), date(2001, 1, 10))), is(false));
        assertThat(predicate.matches(record().set(keyword("dob", Date.class), date(2001, 10, 1))), is(false));
        assertThat(predicate.matches(record().set(keyword("dob", Date.class), date(2001, 1, 10, 3, 15, 59, 123))), is(true));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    @Table ({
            @Row ({"2001/1/10"}),
            @Row ({"10/01/2001"}),
            @Row ({"10/1/2001"}),
            @Row ({"10/01/01"}),
            @Row ({"10/1/01"})
    })
    public void supportsGreaterThanDateQueries(String query) throws Exception {
        Sequence<Keyword<?>> keywords = Sequences.<Keyword<?>>sequence(keyword("dob", Date.class));
        Predicate<Record> predicate = predicateParser.parse(format("dob > %s", query), keywords);

        Keyword<Date> dob = keyword("dob", Date.class);
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 11))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 9))), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    @Table ({
            @Row ({"2001/1/10"}),
            @Row ({"10/01/2001"}),
            @Row ({"10/1/2001"}),
            @Row ({"10/01/01"}),
            @Row ({"10/1/01"})
    })
    public void supportsLowerThanDateQueries(String query) throws Exception {
        Sequence<Keyword<?>> keywords = Sequences.<Keyword<?>>sequence(keyword("dob", Date.class));
        Predicate<Record> predicate = predicateParser.parse(format("dob < %s", query), keywords);

        Keyword<Date> dob = keyword("dob", Date.class);
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 9))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 11))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsLowerThanStringQueries() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name < Dan", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Bob")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsGreaterThanStringQueries() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name > Dan", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bob")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsGreaterThanOrEqualStringQueries() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name >= Dan", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Bob")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsLessThanOrEqualStringQueries() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name <= Dan", Sequences.<Keyword<?>>empty());
        assertThat(predicate.matches(record().set(name, "Bob")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        assertLuceAndSqlSyntax(predicate);
    }

    private static AnsiSqlGrammar grammar = new AnsiSqlGrammar();
    private void assertSqlSyntax(Predicate<Record> predicate) {
        String sql = grammar.toSql(predicate).toString();
        System.out.println("SQL = " + sql);
        assertThat(sql, is(notNullValue()));
    }

    private void assertLuceneSyntax(Predicate<Record> predicate) {
        String luceneQuery = new Lucene(new StringMappings()).query(predicate).toString();
        System.out.println("LUCENE = " + luceneQuery);
        assertThat(luceneQuery, is(notNullValue()));
//        assertThat(predicate.matches(record().set(keyword, 13L)), is(true));
    }

    @Test
    public void shouldCreateOnlyOnePredicateForExplicitQuery() throws Exception {
        Keyword<Long> keyword = keyword("longKeyword", Long.class);
        Predicate<Record> predicate = predicateParser.parse("longKeyword:13", Sequences.<Keyword<?>>sequence(keyword, keyword("unrelatedKeyword", String.class)));
        Predicate<Record> expected = where(keyword, Predicates.is(13L));

        assertThat(predicate, is(expected));
    }

    @Test
    public void shouldCreateNPredicatesForImplicitQuery() throws Exception {
        Keyword<Long> longKeyword = keyword("longKeyword", Long.class);
        Keyword<String> stringKeyword = keyword("stringKeyword", String.class);
        Predicate<Record> predicate = predicateParser.parse("13", Sequences.<Keyword<?>>sequence(longKeyword, stringKeyword));
        Predicate<Record> expected = Predicates.or(Predicates.where(longKeyword, Predicates.is(13L)), Predicates.where(stringKeyword, Predicates.is("13")));

        assertThat(predicate, is(expected));
    }

    @Test
    public void supportsIsNull() throws Exception {
        Keyword<String> keyword = keyword("stringKeyword", String.class);
        Predicate<Record> predicate = predicateParser.parse("stringKeyword:null", Sequences.<Keyword<?>>sequence(keyword));
        Predicate<Record> expected = where(keyword, Predicates.nullValue());

        assertThat(predicate, is(expected));
    }

    @Test
    public void supportsColonAsSeparator() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name:value", Sequences.<Keyword<?>>empty());
        Predicate<Record> expected = Predicates.where(name, Predicates.is("value"));
        assertThat(predicate, is(expected));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsEqualsAsSeparator() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name=value", Sequences.<Keyword<?>>empty());
        Predicate<Record> expected = Predicates.where(name, Predicates.is("value"));
        assertThat(predicate, is(expected));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsContainsWithWildcards() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name=*\"value\"*", Sequences.<Keyword<?>>empty());
        Predicate<Record> expected = Predicates.where(name, Strings.contains("value"));
        assertThat(predicate, is(expected));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsStartsWithWithWildcards() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name=\"value\"*", Sequences.<Keyword<?>>empty());
        Predicate<Record> expected = Predicates.where(name, Strings.startsWith("value"));
        assertThat(predicate, is(expected));

        assertLuceAndSqlSyntax(predicate);
    }

    @Test
    public void supportsEndsWithWithWildcards() throws Exception {
        Predicate<Record> predicate = predicateParser.parse("name=*\"value\"", Sequences.<Keyword<?>>empty());
        Predicate<Record> expected = Predicates.where(name, Strings.endsWith("value"));
        assertThat(predicate, is(expected));

        assertLuceAndSqlSyntax(predicate);
    }
}
