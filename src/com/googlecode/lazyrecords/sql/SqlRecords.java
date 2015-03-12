package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.IgnoreLogger;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.lazyrecords.Queryable;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectBuilder;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.Expressions;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.collections.CloseableList;
import com.googlecode.totallylazy.Computation;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Functions;
import com.googlecode.totallylazy.Iterators;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.googlecode.lazyrecords.Loggers.milliseconds;
import static com.googlecode.lazyrecords.sql.grammars.SqlGrammar.functions.insertStatement;
import static com.googlecode.lazyrecords.sql.grammars.SqlGrammar.functions.updateStatement;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;
import static com.googlecode.totallylazy.collections.CloseableList.constructors.closeableList;
import static com.googlecode.totallylazy.numbers.Numbers.not;
import static com.googlecode.totallylazy.numbers.Numbers.numbers;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class SqlRecords extends AbstractRecords implements Queryable<Expression>, Closeable {
    private final Connection connection;
    private final SqlMappings mappings;
    private final SqlGrammar grammar;
    private final Logger logger;
    private final CloseableList<SqlIterator> closeables = closeableList();

    public SqlRecords(final Connection connection, SqlMappings mappings, SqlGrammar grammar, Logger logger) {
        this.connection = connection;
        this.mappings = mappings;
        this.logger = logger;
        this.grammar = grammar;
    }

    public SqlRecords(final Connection connection) {
        this(connection, new SqlMappings(), new AnsiSqlGrammar(), new IgnoreLogger());
    }

    Connection connection() {
        return connection;
    }

    public void close() throws IOException {
        closeables.close();
    }


    public SqlSequence<Record> get(Definition definition) {
        return new SqlSequence<Record>(this, AnsiSelectBuilder.from(grammar, definition), logger, Functions.<Record>identity());
    }

    public Sequence<Record> query(final Expression expression, final Sequence<Keyword<?>> definitions) {
        return Computation.memorise(closeables.manage(new SqlIterator(connection, mappings, expression, definitions, logger)));
    }

    public Number add(final Definition definition, final Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }
        return update(records.map(insertStatement(grammar, definition)));
    }

    @Override
    public Number set(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return update(records.map(updateStatement(grammar, definition)));
    }

    public Number update(final Expression... expressions) {
        return update(sequence(expressions));
    }

    public Number update(final Sequence<Expression> expressions) {
        if (expressions.isEmpty()) return 0;
        final Map<String, Object> log = Maps.<String, Object>map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.EXPRESSION, expressions));
        long start = System.nanoTime();
        Map<String, PreparedStatement> statements = new HashMap<>();
        try {
            for (Expression expression : expressions) {
                String sql = expression.text();
                Sequence<Object> values = expression.parameters();

                PreparedStatement statement = statements.get(sql);
                if (statement == null) statements.put(sql, statement = connection.prepareStatement(sql));

                mappings.addValues(statement, values);
                statement.addBatch();
            }

            Number rowCount = sequence(statements.values()).map(new Function1<PreparedStatement, Number>() {
                public Number call(PreparedStatement statement) throws Exception {
                    Sequence<Number> counts = numbers(statement.executeBatch());
                    if (counts.contains(Statement.SUCCESS_NO_INFO)) return statement.getUpdateCount();
                    return counts.filter(not(Statement.SUCCESS_NO_INFO)).reduce(sum);
                }
            }.time(milliseconds(log))).reduce(sum);
            log.put(Loggers.ROWS, rowCount);

            return rowCount;
        } catch (SQLException ex) {
            Iterators.each(ex.iterator(), new Block<Throwable>() {
                @Override
                protected void execute(Throwable e) throws Exception {
                    log.put(Loggers.MESSAGE, e.getMessage());
                }
            });
            throw LazyException.lazyException(ex);
        } catch (Exception e) {
            log.put(Loggers.MESSAGE, e.getMessage());
            throw LazyException.lazyException(e);
        } finally {
            for (PreparedStatement statement : statements.values()) safeClose(statement);
            log.put(Loggers.MILLISECONDS, calculateMilliseconds(start, System.nanoTime()));
            logger.log(log);
        }
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        return update(grammar.deleteStatement(definition, Option.some(predicate)));
    }

    public Number remove(Definition definition) {
        return update(grammar.deleteStatement(definition, Option.<Predicate<Record>>none()));
    }
}