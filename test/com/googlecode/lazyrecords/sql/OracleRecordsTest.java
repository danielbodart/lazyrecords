package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Option;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class OracleRecordsTest extends RecordsContract<Records> {
    private SqlRecords sqlRecords;
    private static Option<Connection> connection;

    @BeforeClass
    public static void setupOracle() {
        connection = createConnection();
        org.junit.Assume.assumeTrue(!connection.isEmpty());
    }

    @AfterClass
    public static void shutDown() {
        for (Connection oracle : connection) {
            safeClose(oracle);
        }
    }

    private static Option<Connection> createConnection() {
        try {
            Properties properties = new Properties();
            properties.load(OracleRecordsTest.class.getResourceAsStream("oracle.properties"));
            Class.forName(properties.getProperty("driver"));
            return Option.some(getConnection(properties.getProperty("url"), properties.getProperty("username"), properties.getProperty("password")));
        } catch (Exception e) {
            return Option.none();
        }
    }

    public Records createRecords() throws Exception {
        supportsRowCount = false;
        sqlRecords = new SqlRecords(connection.get(), new SqlMappings(), logger);
        SqlSchema sqlSchema = new SqlSchema(sqlRecords);
        sqlSchema.undefine(people);
        sqlSchema.undefine(books);

        return new SchemaGeneratingRecords(sqlRecords, sqlSchema);
    }

    @Test
    public void supportsDBSequences() throws Exception {
        sqlRecords.update(textOnly("drop sequence foo"));
        sqlRecords.update(textOnly("create sequence foo"));
        Keyword<Integer> nextVal = SqlKeywords.keyword("foo.nextval", Integer.class);
        Definition definition = definition("dual", nextVal);
        Integer integer = records.get(definition).head().get(nextVal);
        assertThat(integer, is(1));
    }
}
