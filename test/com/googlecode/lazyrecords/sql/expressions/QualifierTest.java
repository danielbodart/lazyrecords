package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import org.junit.Test;

import static com.googlecode.lazyrecords.Grammar.eq;
import static com.googlecode.lazyrecords.Grammar.where;
import static com.googlecode.lazyrecords.RecordsContract.People.firstName;
import static com.googlecode.lazyrecords.RecordsContract.People.lastName;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.lazyrecords.sql.expressions.AnsiJoinType.inner;
import static com.googlecode.lazyrecords.sql.expressions.AnsiQualifiedJoin.qualifiedJoin;
import static com.googlecode.lazyrecords.sql.expressions.AnsiTablePrimary.tablePrimary;
import static com.googlecode.lazyrecords.sql.expressions.JoinCondition.joinCondition;
import static com.googlecode.lazyrecords.sql.expressions.NamedColumnsJoin.namedColumnsJoin;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.lazyrecords.sql.expressions.TableName.tableName;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class QualifierTest {
    private static final SqlGrammar grammar = new AnsiSqlGrammar();

    @Test
    public void canQualifyAnSelectExpression() throws Exception {
        SelectExpression expression = from(grammar, people).select(firstName, lastName).distinct().filter(where(firstName, eq("dan"))).build();
        SelectExpression qualified = new Qualifier("t0").qualify(expression);
        assertThat(qualified.toString(), is("select distinct t0.firstName, t0.lastName from people t0 where t0.firstName = 'dan'"));
    }

    @Test
    public void canQualifyAJoinWithUsing() throws Exception {
        QualifiedJoin join = qualifiedJoin(tablePrimary(tableName("people")), inner, tablePrimary(tableName("books")), namedColumnsJoin("isbn"));
        QualifiedJoin qualified = new JoinQualifier("p", "t0").qualify(join);
        assertThat(qualified.toString(), is("people p inner join books t0 using (isbn)"));
    }

    @Test
    public void canQualifyAJoinWithOn() throws Exception {
        QualifiedJoin join = qualifiedJoin(tablePrimary(tableName("people")), inner, tablePrimary(tableName("books")), joinCondition("isbn", "isbn"));
        QualifiedJoin qualified = new JoinQualifier("p", "t0").qualify(join);
        assertThat(qualified.toString(), is("people p inner join books t0 on p.isbn = t0.isbn"));
    }
}
