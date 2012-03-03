package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import org.junit.Ignore;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static java.lang.System.getenv;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("Manual test")
public class OracleRecordsTest extends RecordsContract<SqlRecords> {
    public SqlRecords createRecords() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");

        return new SqlRecords(getConnection(getenv("JDBC_URL"), getenv("JDBC_USERNAME"), getenv("JDBC_PASSWORD")), new SqlMappings(), System.out);
    }

    @Test
    public void supportsDBSequences() throws Exception {
        Keyword<Integer> nextVal = SqlKeywords.keyword("foo.nextval", Integer.class);
        Definition definition = definition("dual", nextVal);
        Integer integer = records.get(definition).head().get(nextVal);
        assertThat(integer, is(notNullValue()));
    }
}
