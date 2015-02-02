package com.googlecode.lazyrecords.sql.expressions;

import org.junit.Test;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class FetchClauseTest {
    @Test
    public void supportsAnsiSql() throws Exception {
        assertThat(new AnsiFetchClause(20).toString(), is("fetch next 20 rows only"));
    }

    @Test
    public void supportsMySql() throws Exception {
        assertThat(new LimitClause(20).toString(), is("limit 20"));
    }
}