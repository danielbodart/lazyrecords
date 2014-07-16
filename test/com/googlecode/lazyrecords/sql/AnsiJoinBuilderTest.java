package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.lazyrecords.sql.expressions.SelectBuilder;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Strings;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Grammar.keyword;
import static com.googlecode.lazyrecords.RecordsContract.Books.books;
import static com.googlecode.lazyrecords.RecordsContract.Books.isbn;
import static com.googlecode.lazyrecords.RecordsContract.Books.title;
import static com.googlecode.lazyrecords.RecordsContract.People.firstName;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.lazyrecords.RecordsContract.Prices.price;
import static com.googlecode.lazyrecords.RecordsContract.Prices.prices;
import static com.googlecode.lazyrecords.sql.AnsiJoinBuilder.join;
import static com.googlecode.lazyrecords.sql.AnsiJoinBuilderTest.Plants.plants;
import static com.googlecode.lazyrecords.sql.expressions.AnsiJoinType.inner;
import static com.googlecode.lazyrecords.sql.expressions.NamedColumnsJoin.namedColumnsJoin;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.totallylazy.Predicates.equalTo;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.capitalise;
import static com.googlecode.totallylazy.comparators.Comparators.ascending;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class AnsiJoinBuilderTest {
    SqlGrammar grammar = new AnsiSqlGrammar();

    @Test
    public void joinsFieldsFromBothTables() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title, isbn);
        AnsiJoinBuilder join = (AnsiJoinBuilder) grammar.join(primary, secondary, inner, namedColumnsJoin("isbn"));
        assertThat(join.fields(), Matchers.<Keyword<?>>containsInAnyOrder(firstName, isbn, title));
    }

    @Test
    public void canJoinOneTable() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title);
        AnsiJoinBuilder join = (AnsiJoinBuilder) grammar.join(primary, secondary, inner, namedColumnsJoin("isbn"));
        assertSql(join, "select p.firstName, p.isbn, b.title from people p inner join books b using (isbn)");
    }

    @Test
    public void canJoinTwoTables() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title);
        SelectBuilder tertiary = from(grammar, prices).select(price);
        AnsiJoinBuilder join = (AnsiJoinBuilder) grammar.join(grammar.join(primary, secondary, inner, namedColumnsJoin("isbn")), tertiary, inner, namedColumnsJoin("isbn"));
        assertSql(join, "select p.firstName, p.isbn, b.title, s.salePrice from people p inner join books b using (isbn) inner join salePrices s using (isbn)");
    }

    @Test
    public void canOrderAfterJoining() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title);
        ExpressionBuilder join = grammar.join(primary, secondary, inner, namedColumnsJoin("isbn")).
            orderBy(ascending(firstName));
        assertSql(join, "select p.firstName, p.isbn, b.title from people p inner join books b using (isbn) order by p.firstName asc");
    }

    @Test
    public void canOrderBeforeJoining() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn).
            orderBy(ascending(firstName));
        SelectBuilder secondary = from(grammar, books).select(title);
        ExpressionBuilder join = grammar.join(primary, secondary, inner, namedColumnsJoin("isbn"));
        assertSql(join, "select p.firstName, p.isbn, b.title from people p inner join books b using (isbn) order by p.firstName asc");
    }

    @Test
    public void canFilterAfterJoining() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title);
        ExpressionBuilder join = grammar.join(primary, secondary, inner, namedColumnsJoin("isbn")).
            filter(where(firstName, Predicates.is("dan")));
        assertSql(join, "select p.firstName, p.isbn, b.title from people p inner join books b using (isbn) where p.firstName = 'dan'");
    }

    @Test
    public void canJoinWithExistingFilter() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title).filter(where(title, equalTo("ray")));
        ExpressionBuilder join = grammar.join(primary, secondary, inner, namedColumnsJoin("isbn"));
        assertSql(join, "select p.firstName, p.isbn, b.title from people p inner join books b using (isbn) where b.title = 'ray'");
    }

    private void assertSql(ExpressionBuilder actual, String expected) {
        assertEquals(expected, actual.build().toString());
    }

    public interface Plants extends Definition {
        Plants plants = definition(Plants.class, capitalise("plants"));
        ImmutableKeyword<String> firstName = keyword("firstName", String.class);
    }

    @Test
    public void canJoinTablesOfDifferentCaseButSameFirstLetter() throws Exception {
        SelectBuilder priamry = from(grammar, people).select(isbn);
        SelectBuilder secondary = from(grammar, plants).select(Plants.firstName);
        AnsiJoinBuilder join = (AnsiJoinBuilder) grammar.join(priamry, secondary, inner, namedColumnsJoin("firstName"));
        String sql = join.build().text();
        assertThat(sql, containsString("join Plants p1"));
    }
}
