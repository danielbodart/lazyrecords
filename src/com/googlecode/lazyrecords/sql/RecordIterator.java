package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
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

import static com.googlecode.lazyrecords.Record.methods.getKeyword;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;
import static com.googlecode.totallylazy.numbers.Numbers.range;

public class RecordIterator extends StatefulIterator<Record> implements Closeable {
    private final Connection connection;
    private final SqlMappings mappings;
    private final Expression expression;
    private final Sequence<Keyword<?>> definitions;
    private final Logger logger;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public RecordIterator(final Connection connection, final SqlMappings mappings, final Expression expression, final Sequence<Keyword<?>> definitions, final Logger logger) {
        this.definitions = definitions;
        this.logger = logger;
        this.connection = connection;
        this.expression = expression;
        this.mappings = mappings;
    }

    @Override
    protected Record getNext() throws Exception {
        ResultSet resultSet = resultSet();
        boolean hasNext = resultSet.next();
        if (!hasNext) {
            close();
            return finished();
        }

        final Record record = Record.constructors.record();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        for (Integer index : range(1).take(metaData.getColumnCount()).safeCast(Integer.class)) {
            final String name = metaData.getColumnLabel(index);
            Keyword<Object> keyword = getKeyword(name, definitions);
            record.set(keyword, mappings.getValue(resultSet, index, keyword.forClass()));
        }

        return record;
    }

    private ResultSet resultSet() throws Exception {
        if (resultSet == null) {
            Map<String, Object> log = Maps.<String, Object>map(pair(Loggers.SQL, expression));
            long start = System.nanoTime();
            preparedStatement = connection.prepareStatement(expression.text());
            mappings.addValues(preparedStatement, expression.parameters());
            resultSet = preparedStatement.executeQuery();
            log.put(Loggers.MILLISECONDS, calculateMilliseconds(start, System.nanoTime()));
            logger.log(log);
        }
        return resultSet;
    }

    public void close() throws IOException {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
