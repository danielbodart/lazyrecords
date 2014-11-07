package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.iterators.StatefulIterator;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;

import static com.googlecode.lazyrecords.Keyword.methods.matchKeyword;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;
import static com.googlecode.totallylazy.numbers.Numbers.range;

public class SqlIterator extends StatefulIterator<Record> implements Closeable {
    private final Connection connection;
    private final SqlMappings mappings;
    private final Expression expression;
    private final Sequence<Keyword<?>> definitions;
    private final Logger logger;
    private final Lazy<PreparedStatement> preparedStatement;
    private final Lazy<ResultSet> resultSet;

    public SqlIterator(final Connection connection, final SqlMappings mappings, final Expression expression, final Sequence<Keyword<?>> definitions, final Logger logger) {
        this.definitions = definitions;
        this.logger = logger;
        this.connection = connection;
        this.expression = expression;
        this.mappings = mappings;
        preparedStatement = new Lazy<PreparedStatement>() {
            @Override
            protected PreparedStatement get() throws Exception {
                return connection.prepareStatement(expression.text());
            }
        };
        resultSet = new Lazy<ResultSet>() {
            @Override
            protected ResultSet get() throws Exception {
                Map<String, Object> log = Maps.<String, Object>map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.EXPRESSION, expression));
                long start = System.nanoTime();
                try {

                    PreparedStatement statement = preparedStatement.value();
                    mappings.addValues(statement, expression.parameters());
                    return statement.executeQuery();
                } catch (Exception e) {
                    log.put(Loggers.MESSAGE, e.getMessage());
                    throw LazyException.lazyException(e);
                } finally {
                    log.put(Loggers.MILLISECONDS, calculateMilliseconds(start, System.nanoTime()));
                    logger.log(log);
                }
            }
        };
    }

    @Override
    protected Record getNext() throws Exception {
        ResultSet result = resultSet.value();
        boolean hasNext = result.next();
        if (!hasNext) {
            close();
            return finished();
        }

        Record record = Record.constructors.record();
        final ResultSetMetaData metaData = result.getMetaData();
        for (Integer index : range(1).take(metaData.getColumnCount()).safeCast(Integer.class)) {
            final String name = metaData.getColumnLabel(index);
            Keyword<Object> keyword = matchKeyword(name, definitions);
            Object value = mappings.getValue(result, index, keyword.forClass());
            if (value != null) {
                record = record.set(keyword, value);
            }
        }

        return record;
    }

    public void close() throws IOException {
        resultSet.close();
        preparedStatement.close();
    }
}
