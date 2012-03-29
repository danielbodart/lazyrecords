package com.googlecode.lazyrecords.sql.expressions;

import org.junit.Test;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class ExpressionsTest {
    @Test
    public void quotesIllegalCharacters() throws Exception {
        assertThat(Expressions.quote("Some table"), is("\"Some table\""));
        assertThat(Expressions.quote("Sometable"), is("Sometable"));
    }
}
