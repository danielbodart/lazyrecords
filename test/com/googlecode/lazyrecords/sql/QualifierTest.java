package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Grammar;
import com.googlecode.lazyrecords.sql.expressions.*;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import org.junit.Test;

import static com.googlecode.lazyrecords.Grammar.where;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.lazyrecords.RecordsContract.People.*;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class QualifierTest {
    private static final SqlGrammar grammar = new AnsiSqlGrammar();

    @Test
    public void canQualifyAnSelectExpression() throws Exception {
        SelectExpression expression = (SelectExpression) from(grammar, people).select(firstName, lastName).distinct().where(where(firstName, Grammar.is("dan"))).build();
        SelectExpression qualified = new Qualifier("t0").qualify(expression);
        assertThat(qualified.toString(), is("select distinct t0.firstName, t0.lastName from t0.people where firstName = 'dan'"));
    }
}
