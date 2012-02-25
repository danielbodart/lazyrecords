package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Schema;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.numbers.Numbers;
import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Queryable;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.Expressions;
import com.googlecode.lazyrecords.sql.mappings.Mappings;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Iterator;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Streams.nullOutputStream;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.sql.expressions.DeleteStatement.deleteStatement;
import static com.googlecode.lazyrecords.sql.expressions.InsertStatement.insertStatement;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.lazyrecords.sql.expressions.UpdateStatement.updateStatement;
import static java.lang.String.format;

public class SqlRecords extends AbstractRecords implements Queryable<Expression>, Closeable {
    private final Connection connection;
    private final PrintStream logger;
    private final Mappings mappings;
    private final CloseableList closeables = new CloseableList();
    private final Schema schema;

    public SqlRecords(final Connection connection, CreateTable createTable, Mappings mappings, PrintStream logger) {
        this.connection = connection;
        this.mappings = mappings;
        this.logger = logger;
        schema = new SqlSchema(this, createTable);
    }

    public SqlRecords(final Connection connection) {
        this(connection, CreateTable.Enabled, new Mappings(), new PrintStream(nullOutputStream()));
    }

    public void close() throws IOException {
        closeables.close();
    }


    public RecordSequence get(Definition definition) {
        return new RecordSequence(this, from(definition), logger);
    }

    public Sequence<Record> query(final Expression expression, final Sequence<Keyword<?>> definitions) {
        return new Sequence<Record>() {
            public Iterator<Record> iterator() {
                return closeables.manage(new RecordIterator(connection, mappings, expression, definitions, logger));
            }
        };
    }

    public Iterator<Record> iterator(Expression expression, Sequence<Keyword<?>> definitions) {
        return query(expression, definitions).iterator();
    }

    public void define(Definition definition) {
        schema.define(definition);
    }

    @Override
    public void undefine(Definition definition) {
        schema.undefine(definition);
    }

    public boolean exists(Definition definition) {
        return schema.exists(definition);
    }

    public Number add(final Definition definition, final Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }
        return update(records.map(insertStatement(definition)));
    }

    @Override
    public Number set(Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return update(records.map(updateStatement(definition)));
    }

    public Number update(final Expression... expressions) {
        return update(sequence(expressions));
    }

    public Number update(final Sequence<Expression> expressions) {
        return expressions.groupBy(Expressions.text()).map(new Callable1<Group<String, Expression>, Number>() {
            public Number call(Group<String, Expression> group) throws Exception {
                logger.print(format("SQL: %s", group.key()));
                Number rowCount = using(connection.prepareStatement(group.key()),
                        mappings.addValuesInBatch(group.map(Expressions.parameters())));
                logger.println(format(" Count:%s", rowCount));
                return rowCount;
            }
        }).reduce(Numbers.sum());
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        return update(deleteStatement(definition, predicate));
    }

    public Number remove(Definition definition) {
        if (!exists(definition)) {
            return 0;
        }
        return update(deleteStatement(definition));
    }

}