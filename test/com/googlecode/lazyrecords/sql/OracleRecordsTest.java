package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.OracleGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Option;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.util.Properties;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.RecordsContract.Books.books;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;
import static com.googlecode.lazyrecords.sql.grammars.ColumnDatatypeMappings.oracle;
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
        for (Connection oracle : connection) safeClose(oracle);
    }

    private static Option<Connection> createConnection() {
        try {
            Properties properties = new Properties();
            properties.load(OracleRecordsTest.class.getResourceAsStream("oracle.properties"));
            Class.forName(properties.getProperty("driver"));
            Connection connection = getConnection(properties.getProperty("url"), properties.getProperty("username"), properties.getProperty("password"));
            connection.createStatement().execute("alter session set current_schema=" + properties.getProperty("schema"));
            return Option.some(connection);
        } catch (Exception e) {
            return Option.none();
        }
    }

    public Records createRecords() throws Exception {
        supportsRowCount = false;
        SqlGrammar grammar = new OracleGrammar();
        sqlRecords = new SqlRecords(connection.get(), new SqlMappings(), grammar, logger);
        SqlSchema sqlSchema = new SqlSchema(sqlRecords, grammar);
        sqlSchema.undefine(people);
        sqlSchema.undefine(books);
        sqlSchema.undefine(Trades.trades);
        return new SchemaGeneratingRecords(sqlRecords, sqlSchema);
    }

    @Test
    public void supportsDBSequences() throws Exception {
        ImmutableKeyword<Integer> nextValue = keyword("nextval", Integer.class);
        Definition foo = Definition.constructors.definition("foo", nextValue);
        Expression sequence = textOnly("sequence").join(textOnly(foo.name()));
        try {
            sqlRecords.update(textOnly("drop").join(sequence));
        } catch (Exception ignore) {
        }
        sqlRecords.update(textOnly("create").join(sequence));
        Definition definition = definition("dual", nextValue.of(foo));
        Integer integer = records.get(definition).head().get(nextValue.of(foo));
        assertThat(integer, is(1));
    }
}
