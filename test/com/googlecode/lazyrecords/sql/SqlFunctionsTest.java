package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.MemoryLogger;
import com.googlecode.lazyrecords.PrintStreamLogger;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.time.Dates;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import static com.googlecode.lazyrecords.Loggers.loggers;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Streams.streams;
import static java.sql.DriverManager.getConnection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SqlFunctionsTest {
    private static Option<Connection> connection;
    private Logger logger = loggers(new MemoryLogger(), new PrintStreamLogger(new PrintStream(streams(System.out, new ByteArrayOutputStream()))));

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
            properties.load(MysqlRecordsTest.class.getResourceAsStream("mysql.properties"));
            Class.forName(properties.getProperty("driver"));
            return Option.some(getConnection(properties.getProperty("url"), properties.getProperty("username"), properties.getProperty("password")));
        } catch (Exception e) {
            return none();
        }
    }

    @Test
    public void supportsIntegerFunctions() throws Exception {
        assertThat(sqlFunctions().get(PrimativeIntegerFunction.class).length("Raymond"), is(7));
        assertThat(sqlFunctions().get(ObjectIntegerFunction.class).length("Jorge"), is(5));
    }

    public interface PrimativeIntegerFunction {
        @SqlFunction("length")
        int length(String value);
    }

    public interface ObjectIntegerFunction {
        @SqlFunction("length")
        Integer length(String value);
    }

    @Test
    public void supportsLongFunctions() {
        assertThat(sqlFunctions().get(PrimativeLongFunction.class).length("Dan"), is(3l));
        assertThat(sqlFunctions().get(ObjectLongFunction.class).length("Stuart"), is(new Long(6l)));
    }

    public interface PrimativeLongFunction {
        @SqlFunction("length")
        long length(String value);
    }

    public interface ObjectLongFunction {
        @SqlFunction("length")
        Long length(String value);
    }

    @Test
    public void supportsDateFunctions() {
        assertThat(sqlFunctions().get(DateFunction.class).date("2003-12-31 01:02:03"), is(Dates.date(2003, 12, 31)));
    }

    public interface DateFunction {
        @SqlFunction("date")
        Date date(String value);
    }

    @Test
    public void supportsTimestampFunctions() {
        assertThat(sqlFunctions().get(TimestampFunction.class).timestamp("2003-12-31 01:02:03"), is(new Timestamp(Dates.date(2003, 12, 31, 1, 2, 3).getTime())));
    }

    public interface TimestampFunction {
        @SqlFunction("timestamp")
        Timestamp timestamp(String value);
    }

    @Test
    public void supportsBooleanFunctions() {
        assertThat(sqlFunctions().get(BooleanFunction.class).isNull(null), is(true));
    }

    public interface BooleanFunction {
        @SqlFunction("isnull")
        Boolean isNull(Object value);
    }

    @Test
    public void supportsBigDecimalFunctions() {
        assertThat(sqlFunctions().get(BigDecimalFunction.class).sqrt(new BigDecimal("20")), is(new BigDecimal("4.47213595499958")));
    }

    public interface BigDecimalFunction {
        @SqlFunction("sqrt")
        BigDecimal sqrt(BigDecimal value);
    }

    @Test
    public void supportsNumberFunctions() {
        assertThat(sqlFunctions().get(NumberFunction.class).ceiling(new BigDecimal("3.14")), is((Number) new BigDecimal("4")));
    }

    public interface NumberFunction {
        @SqlFunction("ceiling")
        Number ceiling(BigDecimal value);
    }

    @Test
    public void supportsStringFunctions() {
        assertThat(sqlFunctions().get(StringFunction.class).lower("SHOUT!"), is("shout!"));
    }

    public interface StringFunction {
        @SqlFunction("lower")
        String lower(String value);
    }

    @Test
    public void supportsManyFunctionsOnSameInterface() {
        final ManyFunctions functions = sqlFunctions().get(ManyFunctions.class);
        assertThat(functions.lower("SHOUT!"), is("shout!"));
        assertThat(functions.upper("quiet!"), is("QUIET!"));
    }

    public interface ManyFunctions {
        @SqlFunction("lower")
        String lower(String value);

        @SqlFunction("upper")
        String upper(String value);
    }

    @Test
    public void supportsDefaultMethodName() {
        assertThat(sqlFunctions().get(DefaultMethodNameFunctions.class).lower("ABC"), is("abc"));
    }

    public interface DefaultMethodNameFunctions {
        @SqlFunction
        String lower(String value);
    }

    private SqlFunctions sqlFunctions() {
        return new SqlFunctions(connection.get(), new SqlMappings(), logger);
    }
}