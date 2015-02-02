package com.googlecode.lazyrecords.sql.expressions;

import org.junit.Test;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class OffsetClauseTest {
    @Test
    public void supportsAnsiSql() throws Exception {
        assertThat(new AnsiOffsetClause(20).toString(), is("offset 20 rows"));
    }

    @Test
    public void supportsMySql() throws Exception {
        assertThat(new MySqlOffsetClause(20).toString(), is("offset 20"));
    }
}