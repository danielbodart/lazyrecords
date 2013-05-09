package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.MemoryLogger;
import com.googlecode.lazyrecords.PrintStreamLogger;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.time.Dates;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import static com.googlecode.lazyrecords.Loggers.loggers;
import static com.googlecode.lazyrecords.sql.MysqlRecordsTest.mySqlConnection;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Streams.streams;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SqlFunctionsTest {
    private static Option<Connection> connection;
    private Logger logger = loggers(new MemoryLogger(), new PrintStreamLogger(new PrintStream(streams(System.out, new ByteArrayOutputStream()))));

    @BeforeClass
    public static void setupOracle() {
        connection = mySqlConnection();
        org.junit.Assume.assumeTrue(!connection.isEmpty());
    }

    @AfterClass
    public static void shutDown() {
        for (Connection dbConnection : connection) safeClose(dbConnection);
    }

    @Test
    public void supportsIntegerFunctions() throws Exception {
        assertThat(sqlFunctions(connection.get(), logger).get(PrimativeIntegerFunction.class).length("Raymond"), is(7));
        assertThat(sqlFunctions(connection.get(), logger).get(ObjectIntegerFunction.class).length("Jorge"), is(5));
    }

    public interface PrimativeIntegerFunction {
        int length(String value);
    }

    public interface ObjectIntegerFunction {
        Integer length(String value);
    }

    @Test
    public void supportsLongFunctions() {
        assertThat(sqlFunctions(connection.get(), logger).get(PrimativeLongFunction.class).length("Dan"), is(3l));
        assertThat(sqlFunctions(connection.get(), logger).get(ObjectLongFunction.class).length("Stuart"), is(new Long(6l)));
    }

    public interface PrimativeLongFunction {
        long length(String value);
    }

    public interface ObjectLongFunction {
        Long length(String value);
    }

    @Test
    public void supportsDateFunctions() {
        assertThat(sqlFunctions(connection.get(), logger).get(DateFunction.class).date("2003-12-31 01:02:03"), is(Dates.date(2003, 12, 31)));
    }

    public interface DateFunction {
        Date date(String value);
    }

    @Test
    public void supportsTimestampFunctions() {
        assertThat(sqlFunctions(connection.get(), logger).get(TimestampFunction.class).timestamp("2003-12-31 01:02:03"), is(new Timestamp(Dates.date(2003, 12, 31, 1, 2, 3).getTime())));
    }

    public interface TimestampFunction {
        Timestamp timestamp(String value);
    }

    @Test
    public void supportsBooleanFunctions() {
        assertThat(sqlFunctions(connection.get(), logger).get(BooleanFunction.class).isNull(null), is(true));
    }

    public interface BooleanFunction {
        @SqlFunction("isnull")
        Boolean isNull(Object value);
    }

    @Test
    public void supportsBigDecimalFunctions() {
        assertThat(sqlFunctions(connection.get(), logger).get(BigDecimalFunction.class).sqrt(new BigDecimal("20")), is(new BigDecimal("4.47213595499958")));
    }

    public interface BigDecimalFunction {
        BigDecimal sqrt(BigDecimal value);
    }

    @Test
    public void supportsNumberFunctions() {
        assertThat(sqlFunctions(connection.get(), logger).get(NumberFunction.class).ceiling(new BigDecimal("3.14")), is((Number) new BigDecimal("4")));
    }

    public interface NumberFunction {
        Number ceiling(BigDecimal value);
    }

    @Test
    public void supportsStringFunctions() {
        assertThat(sqlFunctions(connection.get(), logger).get(StringFunction.class).lower("SHOUT!"), is("shout!"));
    }

    public interface StringFunction {
        String lower(String value);
    }

    @Test
    public void supportsManyFunctionsOnSameInterface() {
        final ManyFunctions functions = sqlFunctions(connection.get(), logger).get(ManyFunctions.class);
        assertThat(functions.lower("SHOUT!"), is("shout!"));
        assertThat(functions.upper("quiet!"), is("QUIET!"));
    }

    public interface ManyFunctions {
        String lower(String value);

        String upper(String value);
    }

    @Test
    public void supportsDefaultMethodName() {
        assertThat(sqlFunctions(connection.get(), logger).get(DefaultMethodNameFunction.class).lower("ABC"), is("abc"));
    }

    public interface DefaultMethodNameFunction {
        String lower(String value);
    }

    @Test
    public void supportsArityGreaterThan1() {
        assertThat(sqlFunctions(connection.get(), logger).get(Artiy2Function.class).substring("Quadratically", 5, 6), is("ratica"));
    }

    public interface Artiy2Function {
        String substring(String value, int pos, int len);
    }

    public static SqlFunctions sqlFunctions(Connection connection, Logger logger) {
        return new SqlFunctions(connection, new SqlMappings(), logger);
    }
}