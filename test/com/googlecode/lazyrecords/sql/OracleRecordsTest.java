package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.grammars.OracleGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Eq;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.annotations.multimethod;
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
import static com.googlecode.lazyrecords.sql.SqlFunctionsTest.sqlFunctions;
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
        connection = oracleConnection();
        org.junit.Assume.assumeTrue(!connection.isEmpty());
    }

    @AfterClass
    public static void shutDown() {
        for (Connection oracle : connection) safeClose(oracle);
    }

    static Option<Connection> oracleConnection() {
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

    @Test
    public void supportsArity0() {
        assertThat(sqlFunctions(connection.get(), logger).get(Artiy0Function.class).user(), is("PENFOLD_OWNER"));
    }

    public interface Artiy0Function {
        String user();
    }

    @Test
    public void supportsSqlFunctionsWithCustomTypes() throws Exception {
        SqlMappings mappings = new SqlMappings().add(MyString.class, new MyStringMapping());
        CustomTypesFunction customTypesFunction = sqlFunctions(connection.get(), logger, mappings).get(CustomTypesFunction.class);
        assertThat(customTypesFunction.trim(new MyString("  cheese  ")), is(new MyString("cheese")));
    }

    public interface CustomTypesFunction {
        MyString trim(MyString value);
    }

    public class MyString extends Eq implements Value<String> {
        private final String value;

        public MyString(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @multimethod
        public boolean equals(MyString other) {
            return this.value.equals(other.value);
        }
        @Override
        public String toString() {
            return value();
        }
    }

    public class MyStringMapping implements StringMapping<MyString> {
        @Override
        public MyString toValue(String value) throws Exception {
            return new MyString(value);
        }

        @Override
        public String toString(MyString value) throws Exception {
            return value.value();
        }
    }

    @Override
    @Ignore
    public void putDoesntRemoveOtherFields() throws Exception {
        // not implemented
    }
}
