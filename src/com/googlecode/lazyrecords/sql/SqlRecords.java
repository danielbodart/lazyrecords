package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.CloseableList;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
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
import java.util.List;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Streams.nullOutputStream;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.sql.expressions.DeleteStatement.deleteStatement;
import static com.googlecode.lazyrecords.sql.expressions.InsertStatement.insertStatement;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.lazyrecords.sql.expressions.TableDefinition.dropTable;
import static com.googlecode.lazyrecords.sql.expressions.TableDefinition.tableDefinition;
import static com.googlecode.lazyrecords.sql.expressions.UpdateStatement.updateStatement;
import static java.lang.String.format;

public class SqlRecords extends AbstractRecords implements Queryable<Expression>, Closeable {
    private final Connection connection;
    private final PrintStream logger;
    private final Mappings mappings;
    private final CreateTable createTable;
    private final CloseableList closeables = new CloseableList();

    public SqlRecords(final Connection connection, CreateTable createTable, Mappings mappings, PrintStream logger) {
        this.connection = connection;
        this.createTable = createTable;
        this.mappings = mappings;
        this.logger = logger;
    }

    public SqlRecords(final Connection connection) {
        this(connection, CreateTable.Enabled, new Mappings(), new PrintStream(nullOutputStream()));
    }

    public void close() throws IOException {
        closeables.close();
    }


    public RecordSequence get(RecordName recordName) {
        return new RecordSequence(this, from(recordName).select(definitions(recordName)), logger);
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

    public void define(RecordName recordName, Keyword<?>... fields) {
        super.define(recordName, fields);
        if(createTable.equals(CreateTable.Disabled)){
            return;
        }
        if (exists(recordName)) {
            return;
        }
        update(tableDefinition(recordName, fields));
    }

    @Override
    public List<Keyword<?>> undefine(RecordName recordName) {
        if(exists(recordName)){
            update(dropTable(recordName));
        }
        return super.undefine(recordName);
    }

    private static final Keyword<Integer> one = keyword("1", Integer.class);

    public boolean exists(RecordName recordName) {
        try {
            query(from(recordName).select(one).where(Predicates.where(one, is(2))).build(), Sequences.<Keyword<?>>empty()).realise();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Number add(final RecordName recordName, final Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }
        return update(records.map(insertStatement(recordName)));
    }

    @Override
    public Number set(RecordName recordName, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return update(records.map(updateStatement(recordName)));
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

    public Number remove(RecordName recordName, Predicate<? super Record> predicate) {
        return update(deleteStatement(recordName, predicate));
    }

    public Number remove(RecordName recordName) {
        if (!exists(recordName)) {
            return 0;
        }
        return update(deleteStatement(recordName));
    }

}