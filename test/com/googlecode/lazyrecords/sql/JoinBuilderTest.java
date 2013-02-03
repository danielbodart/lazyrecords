package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Keyword;
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
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.lazyrecords.sql.JoinBuilder.join;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class JoinBuilderTest {
    SqlGrammar grammar = new AnsiSqlGrammar();

    @Test
    public void joinsFieldsFromBothTables() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title, isbn);
        JoinBuilder join = join(primary, secondary, using(isbn));
        assertThat(join.fields(), Matchers.<Keyword<?>>containsInAnyOrder(firstName, isbn, title));
    }

    @Test
    public void mergesSelect() throws Exception {
        SelectBuilder primary = from(grammar, people).select(firstName, isbn);
        SelectBuilder secondary = from(grammar, books).select(title, isbn);
        JoinBuilder join = join(primary, secondary, using(isbn));
        String sql = join.build().text();
        assertThat(sql, is("select  p.firstName, p.isbn, s0.title, s0.isbn from people p"));
    }

}
