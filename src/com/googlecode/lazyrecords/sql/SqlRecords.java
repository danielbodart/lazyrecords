package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.IgnoreLogger;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.lazyrecords.Queryable;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.Expressions;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Computation;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.numbers.Numbers;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;

import static com.googlecode.lazyrecords.Loggers.milliseconds;
import static com.googlecode.lazyrecords.sql.expressions.DeleteStatement.deleteStatement;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.lazyrecords.sql.grammars.SqlGrammar.functions.insertStatement;
import static com.googlecode.lazyrecords.sql.grammars.SqlGrammar.functions.updateStatement;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.sequence;

public class SqlRecords extends AbstractRecords implements Queryable<Expression>, Closeable {
    private final Connection connection;
    private final SqlMappings mappings;
    private final SqlGrammar grammar;
    private final Logger logger;
    private final CloseableList closeables = new CloseableList();

    public SqlRecords(final Connection connection, SqlMappings mappings, SqlGrammar grammar, Logger logger) {
        this.connection = connection;
        this.mappings = mappings;
        this.logger = logger;
        this.grammar = grammar;
    }

    public SqlRecords(final Connection connection) {
        this(connection, new SqlMappings(), new AnsiSqlGrammar(), new IgnoreLogger());
    }

    public void close() throws IOException {
        closeables.close();
    }


    public SqlSequence get(Definition definition) {
        return new SqlSequence(this, from(grammar, definition), logger);
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
        return expressions.groupBy(Expressions.text()).map(new Callable1<Group<String, Expression>, Number>() {
            public Number call(Group<String, Expression> group) throws Exception {
                Map<String, Object> log = Maps.<String, Object>map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.EXPRESSION, expressions));
                Number rowCount = using(connection.prepareStatement(group.key()),
                        mappings.addValuesInBatch(group.map(Expressions.parameters())).time(milliseconds(log)));
                log.put(Loggers.ROWS, rowCount);
                logger.log(log);
                return rowCount;
            }
        }).reduce(Numbers.sum());
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        return update(grammar.deleteStatement(definition, Option.some(predicate)));
    }

    public Number remove(Definition definition) {
        return update(grammar.deleteStatement(definition, Option.<Predicate<Record>>none()));
    }
}