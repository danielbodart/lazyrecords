package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.AbstractRecordsTests;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.RecordName;
import com.googlecode.lazyrecords.sql.mappings.Mappings;
import org.junit.Test;

import static com.googlecode.lazyrecords.sql.SqlKeywords.keyword;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SqlRecordsTest extends AbstractRecordsTests<SqlRecords> {
    public SqlRecords createRecords() throws Exception {
        return new SqlRecords(getConnection("jdbc:h2:mem:totallylazy", "SA", ""), CreateTable.Enabled, new Mappings(), logger);
    }

    @Test
    public void existsReturnsFalseIfTableNotDefined() throws Exception {
        RecordName sometable = RecordName.recordName("sometable");
        assertThat(records.exists(sometable), is(false));
        records.define(sometable, keyword("id", Integer.class));
        assertThat(records.exists(sometable), is(true));
    }
}
