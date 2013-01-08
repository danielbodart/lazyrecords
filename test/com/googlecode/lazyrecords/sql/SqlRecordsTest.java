package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.matchers.Matchers;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.methods.update;
import static com.googlecode.lazyrecords.Using.using;
import static com.googlecode.lazyrecords.sql.grammars.ColumnDatatypeMappings.hsql;
import static com.googlecode.totallylazy.Predicates.*;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
		records.add(tableWithSpace, Record.constructors.record().set(firstName, "dan").set(age, 12));
		assertThat(records.get(tableWithSpace).map(age).head(), is(12));
		records.set(tableWithSpace, update(using(firstName), Record.constructors.record().set(firstName, "dan").set(age, 11)));
		assertThat(records.get(tableWithSpace).map(age).head(), is(11));
		records.remove(tableWithSpace);
	}

	@Test
	public void supportsSpacesInColumnNames() throws Exception {
		ImmutableKeyword<Integer> age = keyword("my age", Integer.class);
		Definition columnWithSpace = definition("foo", age, firstName);
		records.add(columnWithSpace, Record.constructors.record().set(firstName, "dan").set(age, 12));
		assertThat(records.get(columnWithSpace).map(age).head(), is(12));
		records.set(columnWithSpace, update(using(firstName), Record.constructors.record().set(firstName, "dan").set(age, 11)));
		assertThat(records.get(columnWithSpace).filter(where(age, Predicates.is(11))).map(firstName).head(), is("dan"));
		records.remove(columnWithSpace, always());
	}

	@Test
	public void supportsCountOnSortedRecords() throws Exception {
		assertThat(records.get(people).sortBy(age).size(), NumberMatcher.is(3));
	}

	@Test
	public void memorisesAndThereforeOnlyExecutesSqlOnce() throws Exception {
		MemoryLogger logger = new MemoryLogger();
		Sequence<Record> result = sqlRecords(logger).get(people).sortBy(age);
		Record head = result.head();
		Sequence<Map<String, ?>> logs = logger.data();
		assertThat(head, Matchers.is(result.head())); // Check iterator
		assertThat(logs, Matchers.is(logger.data())); // Check queries
	}

	@Test
	public void correctlyChainsFilterPredicates() {
		Definition table = definition(randomUUID().toString(), firstName, lastName);

		String sameSurname = "bodart";
		Record dan1 = Record.constructors.record()
				.set(firstName, "dan").set(lastName, sameSurname);

		Record dan2 = Record.constructors.record()
				.set(firstName, "dan").set(lastName, "north");

		Record chris = Record.constructors.record()
				.set(firstName, "chris").set(lastName, sameSurname);

		records.add(table,
				dan1,
				dan2,
				chris);

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
		assertThat(
				records.get(people).filter(where(firstName, in(Sequences.empty(String.class)))).size(),
				is(0));
	}

	@Test
	public void memorisesAndThereforeOnlyExecutesSqlOnceEvenWhenYouMapToAKeyword() throws Exception {
		MemoryLogger logger = new MemoryLogger();
		Sequence<String> result = sqlRecords(logger).get(people).map(firstName);
		String head = result.head();
		Sequence<Map<String, ?>> logs = logger.data();
		assertThat(head, Matchers.is(result.head())); // Check iterator
		assertThat(logs, Matchers.is(logger.data())); // Check queries
	}

}
