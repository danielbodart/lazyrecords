package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.Lucene;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.lazyrecords.sql.expressions.WhereClause;
import com.googlecode.totallylazy.predicates.EqualsPredicate;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.predicates.WherePredicate;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.time.Dates.date;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class StandardParserTest {
    @Test
    public void supportsImplicitKeywords() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Keyword<String> name = keyword("name", String.class);
        Predicate<Record> predicate = predicateParser.parse("bob", sequence(name));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "dan")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsExplicitKeywords() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bob", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "dan")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsMultipleConditions() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bob age:12", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "13")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsMultipleConditionsSeparatedByManySpaces() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bob    age:12", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "13")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsNegationWithImplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse(" NOT bob age:12", sequence(keyword("name", String.class)));

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "13")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsNegationWithExplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("-name:bob age:12", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> age = keyword("age", String.class);
        assertThat(predicate.matches(record().set(name, "dan").set(age, "12")), is(true));
        assertThat(predicate.matches(record().set(name, "bob").set(age, "12")), is(false));
        assertThat(predicate.matches(record().set(name, "dan").set(age, "13")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsOrWithImplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("dan,bob", sequence(keyword("name", String.class)));

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);

    }

    @Test
    public void supportsAndWithExplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:bodart AND title:baron", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        Keyword<String> title = keyword("title", String.class);
        assertThat(predicate.matches(record().set(title, "baron").set(name, "bodart")), is(true));
        assertThat(predicate.matches(record().set(title, "duke").set(name, "bodart")), is(false));
        assertThat(predicate.matches(record().set(title, "baron").set(name, "greenback")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsOrWithExplicit() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:dan OR name:bob", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void ignoreWhitespaces() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("  name  :  dan  ,   bob  ", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "dan")), is(true));
        assertThat(predicate.matches(record().set(name, "bob")), is(true));
        assertThat(predicate.matches(record().set(name, "mat")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsQuotedValue() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:\"Dan Bod\"", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bod")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsStartsWith() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:Dan*", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Bod")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsEndsWith() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:*Bod", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan Bod")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bod")), is(true));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsContains() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name:*ell*", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Hello")), is(true));
        assertThat(predicate.matches(record().set(name, "Helo")), is(false));
        assertThat(predicate.matches(record().set(name, "ell")), is(true));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsQuotesContainingNonAlphaNumericCharacters() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("id:\"urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1\"", Sequences.<Keyword<?>>empty());

        Keyword<String> id = keyword("id", String.class);
        assertThat(predicate.matches(record().set(id, "urn:uuid:c356d2c5-f975-4c4d-8e2a-a698158c6ef1")), is(true));
        assertThat(predicate.matches(record().set(id, "fail")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsQuotedName() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("\"First Name\":Dan", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("First Name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsEmptyQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("First Name", String.class);
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));

        assertLuceneSyntax(predicate);
        // SQLRecords guards against ALLPredicate so does not create a where clause at all
    }

    @Test
    public void supportsExplicitDateBasedQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Keyword<Date> dob = keyword("dob", Date.class);
        Predicate<Record> predicate = predicateParser.parse("dob:2001/1/10", Sequences.sequence(dob));

        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 10, 1))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10, 3, 15, 59, 123))), is(true));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsImplicitDateBasedQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Sequence<Keyword<?>> keywords = Sequences.<Keyword<?>>sequence(keyword("dob", Date.class));
        Predicate<Record> predicate = predicateParser.parse("2001/1/10", keywords);

        assertThat(predicate.matches(record().set(keyword("dob", Date.class), date(2001, 1, 10))), is(true));
        assertThat(predicate.matches(record().set(keyword("dob", Date.class), date(2001, 10, 1))), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsGreaterThanQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Sequence<Keyword<?>> keywords = Sequences.<Keyword<?>>sequence(keyword("dob", Date.class));
        Predicate<Record> predicate = predicateParser.parse("dob > 2001/1/10", keywords);

        Keyword<Date> dob = keyword("dob", Date.class);
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 11))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 9))), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsLowerThanDateQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Sequence<Keyword<?>> keywords = Sequences.<Keyword<?>>sequence(keyword("dob", Date.class));
        Predicate<Record> predicate = predicateParser.parse("dob < 2001/1/10", keywords);

        Keyword<Date> dob = keyword("dob", Date.class);
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 9))), is(true));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 11))), is(false));
        assertThat(predicate.matches(record().set(dob, date(2001, 1, 10))), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsLowerThanStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name < Dan", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Bob")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsGreaterThanStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name > Dan", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(false));
        assertThat(predicate.matches(record().set(name, "Bob")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsGreaterThanOrEqualStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name >= Dan", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Mat")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Bob")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    @Test
    public void supportsLessThanOrEqualStringQueries() throws Exception {
        PredicateParser predicateParser = new StandardParser();
        Predicate<Record> predicate = predicateParser.parse("name <= Dan", Sequences.<Keyword<?>>empty());

        Keyword<String> name = keyword("name", String.class);
        assertThat(predicate.matches(record().set(name, "Bob")), is(true));
        assertThat(predicate.matches(record().set(name, "Dan")), is(true));
        assertThat(predicate.matches(record().set(name, "Mat")), is(false));

        assertLuceneSyntax(predicate);
        assertSqlSyntax(predicate);
    }

    private void assertSqlSyntax(Predicate<Record> predicate) {
        String sql = WhereClause.toSql(predicate).toString();
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
        PredicateParser predicateParser = new StandardParser();

        Keyword<Long> keyword = keyword("longKeyword", Long.class);
        Predicate<Record> predicate = predicateParser.parse("longKeyword:13", Sequences.<Keyword<?>>sequence(keyword, keyword("unrelatedKeyword", String.class)));
        Predicate<Record> expected = where(keyword, Predicates.is(13L));

        assertThat(predicate, is(expected));
    }

    @Test
    public void shouldCreateNPredicatesForImplicitQuery() throws Exception {
        PredicateParser predicateParser = new StandardParser();

        Keyword<Long> longKeyword = keyword("longKeyword", Long.class);
        Keyword<String> stringKeyword = keyword("stringKeyword", String.class);
        Predicate<Record> predicate = predicateParser.parse("13", Sequences.<Keyword<?>>sequence(longKeyword, stringKeyword));
        Predicate<Record> expected = Predicates.or(Predicates.where(longKeyword, Predicates.is(13L)), Predicates.where(stringKeyword, Predicates.is("13")));

        assertThat(predicate, is(expected));
    }
}
