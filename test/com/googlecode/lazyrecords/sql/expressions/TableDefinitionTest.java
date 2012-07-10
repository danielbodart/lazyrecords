package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import org.junit.Test;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TableDefinitionTest {
    @Test
    public void theTypeForStringIsVarChar4000() throws Exception {
        assertThat(TableDefinition.type(String.class, AnsiSqlGrammar.defaultMappings()), is("varchar(4000)"));
    }

    @Test
    public void theTypeForAnUnknownTypeIsClob() throws Exception {
        assertThat(TableDefinition.type(Expression.class, AnsiSqlGrammar.defaultMappings()), is("clob"));
    }

}
