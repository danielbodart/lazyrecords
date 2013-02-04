package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.sql.expressions.AnsiJoinType;
import com.googlecode.lazyrecords.sql.expressions.NamedColumnsJoin;
import com.googlecode.lazyrecords.sql.expressions.SelectBuilder;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.googlecode.lazyrecords.RecordsContract.Books.books;
import static com.googlecode.lazyrecords.RecordsContract.Books.isbn;
import static com.googlecode.lazyrecords.RecordsContract.Books.title;
import static com.googlecode.lazyrecords.RecordsContract.People.firstName;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.lazyrecords.RecordsContract.Prices.price;
import static com.googlecode.lazyrecords.RecordsContract.Prices.prices;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.lazyrecords.sql.JoinBuilder.join;
import static com.googlecode.lazyrecords.sql.expressions.AnsiJoinType.inner;
import static com.googlecode.lazyrecords.sql.expressions.NamedColumnsJoin.namedColumnsJoin;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

public class JoinBuilderTest {
    SqlGrammar grammar = new AnsiSqlGrammar();

    @Test
    public void joinsFieldsFromBothTables() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title, isbn);
        JoinBuilder join = join(primary, secondary, inner, namedColumnsJoin("isbn"));
        assertThat(join.fields(), Matchers.<Keyword<?>>containsInAnyOrder(firstName, isbn, title));
    }

    @Test
    public void canJoinOneTable() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title);
        JoinBuilder join = join(primary, secondary, inner, namedColumnsJoin("isbn"));
        String sql = join.build().text();
        assertEquals(sql, "select  p.firstName, p.isbn, s0.title from people p inner join books s0 using (isbn)");
    }

    @Test
    public void canJoinTwoTables() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title);
        SelectBuilder tertiary = from(grammar, prices).select(price);
        JoinBuilder join = join(join(primary, secondary, inner, namedColumnsJoin("isbn")), tertiary, inner, namedColumnsJoin("isbn"));
        String sql = join.build().text();
        assertEquals(sql, "select  p.firstName, p.isbn, s0.title, s1.price from people p inner join books s0 using (isbn) inner join prices s1 using (isbn)");
    }
}
