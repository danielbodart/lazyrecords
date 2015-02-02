package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Aggregate;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Grammar;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.lazyrecords.SchemaGeneratingRecords;
import com.googlecode.lazyrecords.Transaction;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.matchers.Matchers;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Grammar.groupConcat;
import static com.googlecode.lazyrecords.Grammar.to;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.RecordsContract.Books.books;
import static com.googlecode.lazyrecords.RecordsContract.Books.inPrint;
import static com.googlecode.lazyrecords.RecordsContract.Books.isbn;
import static com.googlecode.lazyrecords.RecordsContract.People.age;
import static com.googlecode.lazyrecords.RecordsContract.People.firstName;
import static com.googlecode.lazyrecords.RecordsContract.People.lastName;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.lazyrecords.sql.grammars.ColumnDatatypeMappings.hsql;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.all;
import static com.googlecode.totallylazy.Predicates.always;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class SqlRecordsTest extends RecordsContract<Records> {
    private static JDBCDataSource dataSource;
    private Connection connection;
    private SqlGrammar grammar;

    @BeforeClass
    public static void createDataSource() throws SQLException {
        // HyperSonic: jdbc:hsqldb:mem:totallylazy", "SA", ""
        // H2: jdbc:h2:mem:totallylazy", "SA", ""
        dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:h2:mem:totallylazy");
        dataSource.setUser("SA");
        dataSource.setPassword("");
    }

    @After
    public void closeConnection() throws SQLException {
        connection.close();
    }

    private SqlSchema schema;

    public Records createRecords() throws Exception {
        connection = dataSource.getConnection();
        grammar = new AnsiSqlGrammar(hsql());
        SqlRecords sqlRecords = sqlRecords(logger);
        return new SchemaGeneratingRecords(sqlRecords, schema = new SqlSchema(sqlRecords, grammar));
    }

    private SqlRecords sqlRecords(Logger logger) {
        return new SqlRecords(connection, new SqlMappings(), grammar, logger);
    }

    @Test
    public void supportsReadOnlyConnection() throws Exception {
        Connection readOnlyConnection = new ReadOnlyConnection(dataSource);
        Transaction transaction = new SqlTransaction(readOnlyConnection);
        SqlRecords readOnlyRecords = new SqlRecords(readOnlyConnection);
        assertThat(readOnlyRecords.get(people).size(), NumberMatcher.is(3));
        readOnlyRecords.close();
        transaction.commit();
    }

    @Test
    public void existsReturnsFalseIfTableNotDefined() throws Exception {
        Definition sometable = definition("sometable", age);
        assertThat(schema.exists(sometable), is(false));
        schema.define(sometable);
        assertThat(schema.exists(sometable), is(true));
        schema.undefine(sometable);
        assertThat(schema.exists(sometable), is(false));
    }

    @Test
    public void supportsSpacesInTableNames() throws Exception {
        Definition tableWithSpace = definition("some table", age, firstName);
        records.add(tableWithSpace, record().set(firstName, "dan").set(age, 12));
        assertThat(records.get(tableWithSpace).map(age).head(), is(12));
        records.set(tableWithSpace, update(using(firstName), record().set(firstName, "dan").set(age, 11)));
        assertThat(records.get(tableWithSpace).map(age).head(), is(11));
        records.remove(tableWithSpace);
    }

    @Test
    public void supportsSpacesInColumnNames() throws Exception {
        ImmutableKeyword<Integer> age = keyword("my age", Integer.class);
        Definition columnWithSpace = definition("foo", age, firstName);
        records.remove(columnWithSpace, always());
        records.add(columnWithSpace, record().set(firstName, "dan").set(age, 12));
        assertThat(records.get(columnWithSpace).map(age).head(), is(12));
        records.set(columnWithSpace, update(using(firstName), record().set(firstName, "dan").set(age, 11)));
        assertThat(records.get(columnWithSpace).filter(where(age, Predicates.is(11))).map(firstName).head(), is("dan"));
        records.remove(columnWithSpace, always());
    }

    @Test
    public void supportsCountOnSortedRecords() throws Exception {
        assertThat(records.get(people).sortBy(age).size(), NumberMatcher.is(3));
    }

    @Test
    public void memorisesAndThereforeOnlyExecutesSqlOnce() throws Exception {
        memory.forget();
        Sequence<Record> result = records.get(people).sortBy(age);
        Record head = result.head();
        Sequence<Map<String, ?>> logs = memory.data();
        assertThat(head, Matchers.is(result.head())); // Check iterator
        assertThat(logs, Matchers.is(memory.data())); // Check queries
    }

    @Test
    public void correctlyChainsFilterPredicates() {
        Definition table = definition(randomUUID().toString(), firstName, lastName);
        records.remove(table);

        String sameSurname = "bodart";
        Record dan1 = record(firstName, "dan", lastName, sameSurname);
        Record dan2 = record(firstName, "dan", lastName, "north");
        Record chris = record(firstName, "chris", lastName, sameSurname);

        records.add(table, dan1, dan2, chris);

        assertThat(records.get(table)
                .filter(where(lastName, Predicates.is(sameSurname)))
                .filter(where(firstName, Predicates.is("dan")))
                .size(),
                is(1));
    }

    @Test
    public void addingTheAllPredicateAsAnOrClauseReturnsAllRecords() {
        assertThat(
                records.get(people).filter(where(firstName, Predicates.is("dan")).or(all())).size(),
                is(3));
    }

    @Test
    public void supportsWherePredicateUsingAllPredicate() {
        assertThat(
                records.get(people).filter(where(firstName, all())).size(),
                is(3));
    }

    @Test
    public void supportsEmptyInPredicates() {
        assertThat(records.get(people).filter(where(firstName, in(empty(String.class)))).size(),
                is(0));
    }

    @Test
    public void memorisesAndThereforeOnlyExecutesSqlOnceEvenWhenYouMapToAKeyword() throws Exception {
        memory.forget();
        Sequence<String> result = records.get(people).map(firstName);
        String head = result.head();
        Sequence<Map<String, ?>> logs = memory.data();
        assertThat(head, Matchers.is(result.head())); // Check iterator
        assertThat(logs, Matchers.is(memory.data())); // Check queries
    }

    @Test @Override
    public void joinUsingWithOutSelectingReturnsAllFieldsFromBothByDefault() throws Exception {
        super.joinUsingWithOutSelectingReturnsAllFieldsFromBothByDefault();
        assertSql("select p.age, p.dob, p.firstName, p.lastName, p.isbn, b.isbn, b.title, b.inPrint, b.uuid, b.rrp " +
                "from people p inner join books b using (isbn) " +
                "where p.age < '12'");
    }

    @Test @Override
    public void joinUsingMergesPreviouslySelectedFields() throws Exception {
        super.joinUsingMergesPreviouslySelectedFields();
        assertSql("select p.isbn, p.age, b.title, b.isbn " +
                "from people p inner join books b using (isbn) " +
                "where p.age < '12'");
    }

    @Test @Override
    public void canSelectFieldsAfterJoining() throws Exception {
        super.canSelectFieldsAfterJoining();
        assertSql("select p.firstName, p.isbn " +
                "from people p inner join books b using (isbn) " +
                "where p.age < '12'");
    }

    @Test @Override
    public void supportsJoinOn() throws Exception {
        super.supportsJoinOn();
        assertSql("select p.age, p.dob, p.firstName, p.lastName, p.isbn, p1.book, p1.price " +
                "from people p left join prices p1 on p.isbn = p1.book " +
                "where p.firstName = 'matt'");
    }

    @Test @Override
    public void shouldSupportGroupConcatFunction() throws Exception {
        final Aggregate<URI, String> concatIsbn = groupConcat(isbn);
        final Sequence<Record> recordResults = records.get(books).groupBy(inPrint).map(Grammar.reduce(to(concatIsbn))).realise();
        assertThat(recordResults.head() instanceof Record, org.hamcrest.Matchers.is(true));
        final Sequence<String> results = recordResults.map(concatIsbn);
        assertThat(results, containsInAnyOrder("urn:isbn:0099322617,urn:isbn:0132350882", "urn:isbn:0140289208"));
    }

    @Test @Override
    public void canCombineDropAndTake() throws Exception {
        assertThat(records.get(people).sortBy(firstName).drop(1).take(1).size(), NumberMatcher.is(1));
        // TODO Fix so it does it like LuceneSequence
//        assertThat(records.get(people).sortBy(firstName).take(2).drop(1).size(), NumberMatcher.is(1));
//        assertThat(records.get(people).sortBy(firstName).drop(1).take(2).drop(1).map(firstName), hasExactly("matt"));
    }


    protected void assertSql(final String expected) {
        assertEquals(expected, sql());
    }

    protected String sql() {
        return memory.data().head().get("expression").toString();
    }

    @Override
    @Ignore
    public void putDoesntRemoveOtherFields() throws Exception {
        // not implemented
    }
}