package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.RecordDefinition;
import com.googlecode.lazyrecords.SchemaBasedRecordContract;
import com.googlecode.lazyrecords.sql.mappings.Mappings;
import org.junit.Test;

import static com.googlecode.lazyrecords.sql.SqlKeywords.keyword;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SqlRecordsTest extends SchemaBasedRecordContract<SqlRecords> {
    public SqlRecords createRecords() throws Exception {
        SqlRecords sqlRecords = new SqlRecords(getConnection("jdbc:h2:mem:totallylazy", "SA", ""), CreateTable.Enabled, new Mappings(), logger);
        schema = sqlRecords.schema();
        return sqlRecords;
    }

    @Test
    public void existsReturnsFalseIfTableNotDefined() throws Exception {
        Definition sometable = RecordDefinition.definition("sometable", age);
        assertThat(schema.exists(sometable), is(false));
        schema.define(sometable);
        assertThat(schema.exists(sometable), is(true));
    }
}
