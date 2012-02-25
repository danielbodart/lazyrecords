package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.RecordDefinition;
import com.googlecode.lazyrecords.sql.mappings.Mappings;
import org.junit.Test;

import static com.googlecode.lazyrecords.sql.SqlKeywords.keyword;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SqlRecordsTest extends RecordsContract<SqlRecords> {
    public SqlRecords createRecords() throws Exception {
        return new SqlRecords(getConnection("jdbc:h2:mem:totallylazy", "SA", ""), CreateTable.Enabled, new Mappings(), logger);
    }

    @Test
    public void existsReturnsFalseIfTableNotDefined() throws Exception {
        Definition sometable = RecordDefinition.definition("sometable");
        assertThat(records.exists(sometable), is(false));
        records.define(sometable);
        assertThat(records.exists(sometable), is(true));
    }
}
