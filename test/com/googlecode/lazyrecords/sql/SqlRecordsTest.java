package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Predicates;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Predicates.always;
import static com.googlecode.totallylazy.Predicates.where;
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
        schema.undefine(sometable);
        assertThat(schema.exists(sometable), is(false));
    }

    @Test
    public void supportsSpacesInTableNames() throws Exception {
        Definition tableWithSpace = definition("some table", age, firstName);
        records.add(tableWithSpace, record().set(firstName, "dan").set(age, 12));
        assertThat(records.get(tableWithSpace).map(age).head(), is(12));
        records.set(tableWithSpace, update(using(firstName), record().set(firstName, "dan").set(age, 11)));
        assertThat(records.get(tableWithSpace).map(age).head(), is(11));
        records.remove(tableWithSpace);
    }

    @Test
    public void supportsSpacesInColumnNames() throws Exception {
        ImmutableKeyword<Integer> age = keyword("my age", Integer.class);
        Definition columnWithSpace = definition("foo", age, firstName);
        records.add(columnWithSpace, record().set(firstName, "dan").set(age, 12));
        assertThat(records.get(columnWithSpace).map(age).head(), is(12));
        records.set(columnWithSpace, update(using(firstName), record().set(firstName, "dan").set(age, 11)));
        assertThat(records.get(columnWithSpace).filter(where(age, Predicates.is(11))).map(firstName).head(), is("dan"));
        records.remove(columnWithSpace, always());
    }
}
