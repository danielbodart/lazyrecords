package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.ColumnDatatypeMappings;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Option;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.util.Properties;

import static com.googlecode.lazyrecords.RecordsContract.Books.books;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Option.none;
import static java.sql.DriverManager.getConnection;

public class MysqlRecordsTest extends RecordsContract<Records> {
    private SqlRecords sqlRecords;
    private static Option<Connection> connection = none();

    @BeforeClass
    public static void setupMysql() {
        connection = createConnection();
        org.junit.Assume.assumeTrue(!connection.isEmpty());
    }

    @AfterClass
    public static void shutDown() {
        for (Connection mysql : connection)
            safeClose(mysql);
    }

    static Option<Connection> createConnection() {
        try {
            Properties properties = new Properties();
            properties.load(MysqlRecordsTest.class.getResourceAsStream("mysql.properties"));
            Class.forName(properties.getProperty("driver"));
            return Option.some(getConnection(properties.getProperty("url"), properties.getProperty("username"), properties.getProperty("password")));
        } catch (Exception e) {
            return none();
        }
    }

    public Records createRecords() throws Exception {
        supportsRowCount = true;
        SqlGrammar grammar = new AnsiSqlGrammar(ColumnDatatypeMappings.mysql());
        sqlRecords = new SqlRecords(connection.get(), new SqlMappings(), grammar, logger);
        SqlSchema sqlSchema = new SqlSchema(sqlRecords, grammar);
        sqlSchema.undefine(people);
        sqlSchema.undefine(books);
        return new SchemaGeneratingRecords(sqlRecords, sqlSchema);
    }
}
