package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SqlRecordsTest extends RecordsContract<Records> {
    private SqlSchema schema;

    public Records createRecords() throws Exception {
        SqlRecords sqlRecords = new SqlRecords(
                getConnection("jdbc:h2:mem:totallylazy", "SA", ""),
                new SqlMappings(),
                logger);
        return new SchemaGeneratingRecords(sqlRecords, schema = new SqlSchema(sqlRecords));
    }

    @Test
    public void existsReturnsFalseIfTableNotDefined() throws Exception {
        Definition sometable = definition("sometable", age);
        assertThat(schema.exists(sometable), is(false));
        schema.define(sometable);
        assertThat(schema.exists(sometable), is(true));
    }
}
