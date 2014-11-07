package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
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
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;
import static com.googlecode.totallylazy.numbers.Numbers.range;

public class SqlIterator extends StatefulIterator<Record> implements Closeable {
    private final SqlMappings mappings;
    private final Lazy<PreparedStatement> preparedStatement;
    private final Lazy<ResultSet> resultSet;
    private final Lazy<Sequence<Pair<Integer, Keyword<Object>>>> keywords;

    public SqlIterator(final Connection connection, final SqlMappings mappings, final Expression expression, final Sequence<Keyword<?>> definitions, final Logger logger) {
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
        keywords = new Lazy<Sequence<Pair<Integer, Keyword<Object>>>>() {
            @Override
            protected Sequence<Pair<Integer, Keyword<Object>>> get() throws Exception {
                final ResultSetMetaData metaData = resultSet.value().getMetaData();
                return range(1).take(metaData.getColumnCount()).safeCast(Integer.class).map(new Function1<Integer, Pair<Integer, Keyword<Object>>>() {
                    @Override
                    public Pair<Integer, Keyword<Object>> call(Integer index) throws Exception {
                        final String name = metaData.getColumnLabel(index);
                        return pair(index, matchKeyword(name, definitions));
                    }
                }).unique(Callables.<Keyword<Object>>second()).realise();
            }
        };
    }

    @Override
    protected Record getNext() throws Exception {
        final ResultSet result = resultSet.value();
        boolean hasNext = result.next();
        if (!hasNext) {
            close();
            return finished();
        }

        return record(keywords.value().map(new Function1<Pair<Integer, Keyword<Object>>, Pair<Keyword<Object>, Object>>() {
            @Override
            public Pair<Keyword<Object>, Object> call(Pair<Integer, Keyword<Object>> pair) throws Exception {
                Keyword<Object> keyword = pair.second();
                Integer index = pair.first();
                return pair(keyword, mappings.getValue(result, index, keyword.forClass()));
            }
        }).filter(where(second(Object.class), notNullValue())));
    }

    public void close() throws IOException {
        resultSet.close();
        preparedStatement.close();
    }
}