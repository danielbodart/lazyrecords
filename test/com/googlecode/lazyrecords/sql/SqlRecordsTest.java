package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.totallylazy.Predicates.always;
import static com.googlecode.totallylazy.Predicates.where;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SqlRecordsTest extends RecordsContract<Records> {
    private static JdbcConnectionPool dataSource;
    private Connection connection;

    @BeforeClass
    public static void createDataSource() {
    // HyperSonic: jdbc:hsqldb:mem:totallylazy", "SA", ""
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:totallylazy", "SA", "");
    }

    @AfterClass
    public static void closeDataSource() {
        dataSource.dispose();
    }

    @After
    public void closeConnection() throws SQLException {
        connection.close();
    }
    private SqlSchema schema;

    public Records createRecords() throws Exception {
        connection = dataSource.getConnection();
        SqlRecords sqlRecords = new SqlRecords(connection,
                new SqlMappings(), logger);
        return new SchemaGeneratingRecords(sqlRecords, schema = new SqlSchema(sqlRecords));
    }

    @Test
    public void supportsReadOnlyConnection() throws Exception {
        Connection readOnlyConnection = new ReadOnlyConnection(dataSource);
        Transaction transaction = new SqlTransaction(readOnlyConnection);
        SqlRecords readOnlyRecords = new SqlRecords(readOnlyConnection);
        assertThat(readOnlyRecords.get(people).size(), NumberMatcher.is(3));
        readOnlyRecords.close();
        transaction.commit();
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

    @Test
    public void supportsCountOnSortedRecords() throws Exception {
        assertThat(records.get(people).sortBy(age).size(), NumberMatcher.is(3));
    }
}
