package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.AbstractRecordsTests;
import com.googlecode.lazyrecords.RecordDefinition;
import com.googlecode.lazyrecords.sql.mappings.Mappings;
import org.junit.Ignore;
import org.junit.Test;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static java.lang.System.getenv;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore("Manual test")
public class OracleRecordsTest extends AbstractRecordsTests<SqlRecords> {
    public SqlRecords createRecords() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");

        return new SqlRecords(getConnection(getenv("JDBC_URL"), getenv("JDBC_USERNAME"), getenv("JDBC_PASSWORD")), CreateTable.Enabled, new Mappings(), System.out);
    }

    @Test
    public void supportsDBSequences() throws Exception {
        Integer integer = records.get(RecordDefinition.definition("dual")).map(SqlKeywords.keyword("foo.nextval", Integer.class)).head();
        assertThat(integer, is(notNullValue()));
    }
}
