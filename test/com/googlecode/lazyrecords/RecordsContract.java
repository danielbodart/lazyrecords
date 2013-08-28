package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.matchers.IterableMatcher;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import static com.googlecode.lazyrecords.Grammar.all;
import static com.googlecode.lazyrecords.Grammar.ascending;
import static com.googlecode.lazyrecords.Grammar.average;
import static com.googlecode.lazyrecords.Grammar.between;
import static com.googlecode.lazyrecords.Grammar.concat;
import static com.googlecode.lazyrecords.Grammar.contains;
import static com.googlecode.lazyrecords.Grammar.count;
import static com.googlecode.lazyrecords.Grammar.definition;
import static com.googlecode.lazyrecords.Grammar.descending;
import static com.googlecode.lazyrecords.Grammar.endsWith;
import static com.googlecode.lazyrecords.Grammar.greaterThan;
import static com.googlecode.lazyrecords.Grammar.greaterThanOrEqualTo;
import static com.googlecode.lazyrecords.Grammar.in;
import static com.googlecode.lazyrecords.Grammar.is;
import static com.googlecode.lazyrecords.Grammar.join;
import static com.googlecode.lazyrecords.Grammar.keyword;
import static com.googlecode.lazyrecords.Grammar.lessThan;
import static com.googlecode.lazyrecords.Grammar.lessThanOrEqualTo;
import static com.googlecode.lazyrecords.Grammar.maximum;
import static com.googlecode.lazyrecords.Grammar.minimum;
import static com.googlecode.lazyrecords.Grammar.not;
import static com.googlecode.lazyrecords.Grammar.notNullValue;
import static com.googlecode.lazyrecords.Grammar.nullValue;
import static com.googlecode.lazyrecords.Grammar.on;
import static com.googlecode.lazyrecords.Grammar.outerJoin;
import static com.googlecode.lazyrecords.Grammar.record;
import static com.googlecode.lazyrecords.Grammar.select;
import static com.googlecode.lazyrecords.Grammar.startsWith;
import static com.googlecode.lazyrecords.Grammar.sum;
import static com.googlecode.lazyrecords.Grammar.to;
import static com.googlecode.lazyrecords.Grammar.update;
import static com.googlecode.lazyrecords.Grammar.using;
import static com.googlecode.lazyrecords.Grammar.where;
import static com.googlecode.lazyrecords.Loggers.loggers;
import static com.googlecode.lazyrecords.RecordsContract.Books.books;
import static com.googlecode.lazyrecords.RecordsContract.Books.inPrint;
import static com.googlecode.lazyrecords.RecordsContract.Books.isbn;
import static com.googlecode.lazyrecords.RecordsContract.Books.rrp;
import static com.googlecode.lazyrecords.RecordsContract.Books.title;
import static com.googlecode.lazyrecords.RecordsContract.Books.uuid;
import static com.googlecode.lazyrecords.RecordsContract.People.age;
import static com.googlecode.lazyrecords.RecordsContract.People.dob;
import static com.googlecode.lazyrecords.RecordsContract.People.firstName;
import static com.googlecode.lazyrecords.RecordsContract.People.lastName;
import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.lazyrecords.RecordsContract.Prices.price;
import static com.googlecode.lazyrecords.RecordsContract.Prices.prices;
import static com.googlecode.lazyrecords.RecordsContract.Trades.approverId;
import static com.googlecode.lazyrecords.RecordsContract.Trades.creatorId;
import static com.googlecode.lazyrecords.RecordsContract.Trades.trades;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Streams.streams;
import static com.googlecode.totallylazy.URLs.uri;
import static com.googlecode.totallylazy.comparators.Comparators.comparators;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.matchers.Matchers.matcher;
import static com.googlecode.totallylazy.matchers.NumberMatcher.equalTo;
import static com.googlecode.totallylazy.time.Dates.date;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.fail;

public abstract class RecordsContract<T extends Records> {
    public interface Books extends Definition {
        Books books = definition(Books.class);
        Keyword<URI> isbn = keyword("isbn", URI.class);
        Keyword<String> title = keyword("title", String.class);
        Keyword<Boolean> inPrint = keyword("inPrint", Boolean.class);
        Keyword<UUID> uuid = keyword("uuid", UUID.class);
        Keyword<BigDecimal> rrp = keyword("rrp", BigDecimal.class);
    }

    public interface People extends Definition {
        People people = definition(People.class);
        Keyword<Integer> age = keyword("age", Integer.class);
        Keyword<Date> dob = keyword("dob", Date.class);
        ImmutableKeyword<String> firstName = keyword("firstName", String.class);
        ImmutableKeyword<String> lastName = keyword("lastName", String.class);
        Keyword<URI> isbn = Books.isbn;
    }

    public static final URI zenIsbn = uri("urn:isbn:0099322617");
    public static final URI godelEsherBach = uri("urn:isbn:0140289208");
    public static final URI cleanCode = uri("urn:isbn:0132350882");
    public static final UUID zenUuid = randomUUID();

    protected T records;

    protected Logger logger;
    protected MemoryLogger memory;
    protected ByteArrayOutputStream stream;
    protected boolean supportsRowCount = true;

    protected String popLog() {
        String result = stream.toString();
        stream.reset();
        return result;
    }

    protected abstract T createRecords() throws Exception;

    @Before
    public void setupRecords() throws Exception {
        stream = new ByteArrayOutputStream();
        memory = new MemoryLogger();
        logger = loggers(memory, new PrintStreamLogger(new PrintStream(streams(System.out, stream))));
        this.records = createRecords();
        setupData();
    }

    protected void setupData() {
        setupPeople();
        setupBooks();
    }

    @After
    public void cleanUp() throws Exception {
        Closeables.close(records);
    }

    public String log() {
        return stream.toString();
    }

    public void assertCount(Number actual, Number expected) {
        if (supportsRowCount) {
            assertThat(actual, NumberMatcher.is(expected));
        }
    }

    private void setupPeople() {
        records.remove(people);
        records.add(people,
                record(firstName, "dan", lastName, "bodart", age, 9, dob, date(1977, 1, 10), isbn, zenIsbn),
                record(firstName, "matt", lastName, "savage", age, 12, dob, date(1975, 1, 10), isbn, godelEsherBach),
                record(firstName, "bob", lastName, "martin", age, 11, dob, date(1976, 1, 10), isbn, cleanCode));
    }

    private void setupBooks() {
        records.remove(books);
        records.add(books,
                record(isbn, zenIsbn, title, "Zen And The Art Of Motorcycle Maintenance", inPrint, true, uuid, zenUuid, rrp, new BigDecimal("9.95")),
                record(isbn, godelEsherBach, title, "Godel, Escher, Bach: An Eternal Golden Braid", inPrint, false, uuid, randomUUID(), rrp, new BigDecimal("20.00")),
                record(isbn, cleanCode, title, "Clean Code: A Handbook of Agile Software Craftsmanship", inPrint, true, uuid, randomUUID(), rrp, new BigDecimal("34.99")));
    }

    @Test
    public void supportsAllPredicate() throws Exception {
        assertThat(records.get(people).filter(all()).size(), NumberMatcher.is(3));
    }

    @Test
    public void supportsCorrectlySortingNumbers() throws Exception {
        records.add(people,
                record(firstName, "great", lastName, "grandfather", age, 100, dob, date(1877, 1, 10), isbn, zenIsbn));
        assertThat(records.get(people).filter(where(age, is(notNullValue(Integer.class)))).sortBy(descending(age)).map(age), hasExactly(100, 12, 11, 9));
    }

    @Test
    public void supportsSortingByMultipleKeywords() throws Exception {
        records.add(people,
                record(firstName, "aaaalfred", lastName, "bodart", age, 10, dob, date(1977, 1, 10), isbn, zenIsbn));
        assertThat(
                records.get(people).filter(where(age, is(notNullValue(Integer.class)))).sortBy(comparators(descending(age), ascending(firstName))).map(firstName),
                hasExactly("matt", "bob", "aaaalfred", "dan"));
    }

    @Test
    public void joinUsingWithOutSelectingReturnsAllFieldsFromBothByDefault() throws Exception {
        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(join(records.get(books), using(isbn))).
                head().fields().size(), NumberMatcher.is(9));
    }

    @Test
    public void joinUsingMergesPreviouslySelectedFields() throws Exception {
        assertThat(records.get(people).map(select(isbn, age)).filter(where(age, is(lessThan(12)))).
                flatMap(join(records.get(books).map(select(title, isbn)), using(isbn))).
                head().fields().size(), NumberMatcher.is(3));
    }

    @Test
    public void canSelectFieldsAfterJoining() throws Exception {
        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(join(records.get(books), using(isbn))).
                map(select(firstName, isbn)).
                head().fields().size(), NumberMatcher.is(2));
    }

    @Test
    public void supportsJoinOn() throws Exception {
        Keyword<BigDecimal> price = keyword("price", BigDecimal.class);
        Keyword<URI> book = keyword("book", URI.class);
        Definition prices = definition("prices", book, price);

        records.remove(prices);
        records.add(prices, record(book, zenIsbn, price, new BigDecimal("4.95")));

        Sequence<Record> peopleAndSalePrices = records.get(people).
                flatMap(outerJoin(records.get(prices), on(isbn, book)));

        Record dansFavouriteBook = peopleAndSalePrices.filter(where(firstName, is("dan"))).head();
        assertThat(dansFavouriteBook.get(firstName), Matchers.is("dan"));
        assertThat(dansFavouriteBook.get(price), matcher(between(new BigDecimal("4.95"), new BigDecimal("4.95"))));

        Record mattsFavouriteBook = peopleAndSalePrices.filter(where(firstName, is("matt"))).head();
        assertThat(mattsFavouriteBook.get(firstName), Matchers.is("matt"));
        assertThat(mattsFavouriteBook.get(price), Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void supportsOuterJoinUsing() throws Exception {
        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(outerJoin(records.get(books), using(isbn))).
                head().fields().size(), NumberMatcher.is(9));

        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(outerJoin(records.get(books), using(isbn))).
                map(select(firstName, isbn)).
                head().fields().size(), NumberMatcher.is(2));

        assertThat(records.get(people).map(select(isbn, age)).filter(where(age, is(lessThan(12)))).
                flatMap(outerJoin(records.get(books).map(select(title, isbn)), using(isbn))).
                head().fields().size(), NumberMatcher.is(3));

        Record personWithNoCorrespondingBook = record(firstName, "ray", lastName, "barlow", age, 9, dob, date(1977, 1, 10), isbn, uri("urn:isbn:0000000000"));
        records.remove(people);
        records.add(people, personWithNoCorrespondingBook);

        assertThat(records.get(people).map(select(isbn, firstName)).filter(where(firstName, is("ray"))).
                flatMap(outerJoin(records.get(books).map(select(title, isbn)), using(isbn))).
                head().get(firstName), Matchers.is("ray"));
    }

    public interface Prices extends Definition {
        Prices prices = definition(Prices.class, "salePrices");
        Keyword<BigDecimal> price = keyword("salePrice", BigDecimal.class);
        Keyword<URI> isbn = Books.isbn;
    }

    @Test
    public void supportsJoiningAcrossMoreThanTwoTables() {
        records.remove(prices);
        records.add(prices, record(Prices.isbn, zenIsbn, price, new BigDecimal("4.95")));

        Sequence<Record> peopleAndBooksAndSalePrices = records.get(people).
                flatMap(outerJoin(records.get(books), using(isbn))).
                flatMap(outerJoin(records.get(prices), using(isbn)));

        Record dansFavouriteBook = peopleAndBooksAndSalePrices.filter(where(firstName, Grammar.is("dan"))).head();
        assertThat(dansFavouriteBook.get(firstName), Matchers.is("dan"));
        assertThat(dansFavouriteBook.get(title), Matchers.is("Zen And The Art Of Motorcycle Maintenance"));
        assertThat(dansFavouriteBook.get(price), matcher(between(new BigDecimal("4.95"), new BigDecimal("4.95"))));

        Record mattsFavouriteBook = peopleAndBooksAndSalePrices.filter(where(firstName, Grammar.is("matt"))).head();
        assertThat(mattsFavouriteBook.get(firstName), Matchers.is("matt"));
        assertThat(mattsFavouriteBook.get(title), Matchers.is("Godel, Escher, Bach: An Eternal Golden Braid"));
        assertThat(mattsFavouriteBook.get(price), Matchers.is(Matchers.nullValue()));
    }

    public interface Trades extends Definition {
        Definition trades = definition(Trades.class);
        Keyword<BigDecimal> price = Prices.price;
        Keyword<String> creatorId = keyword("creator_id", String.class);
        Keyword<String> approverId = keyword("approver_id", String.class);
    }

    @Test
    public void supportsJoiningOnSameTableMultipleTimes() {
        records.remove(trades);
        records.add(trades, record(creatorId, "dan", approverId, "matt", price, new BigDecimal("4.95")));

        Keyword<String> creator = lastName.as("creator");
        Keyword<String> approver = lastName.as("approver");
        Record tradeDetails = records.get(trades).
                flatMap(outerJoin(records.get(people).map(select(firstName, creator)), on(creatorId, firstName))).
                flatMap(outerJoin(records.get(people).map(select(firstName, approver)), on(approverId, firstName))).head();

        assertThat(tradeDetails.get(creator), Matchers.is("bodart"));
        assertThat(tradeDetails.get(price), matcher(between(new BigDecimal("4.95"), new BigDecimal("4.95"))));
        assertThat(tradeDetails.get(approver), Matchers.is("savage"));
    }

    @Test
    public void supportsJoiningOnToSameTable() {
        records.remove(prices);
        records.add(prices, record(isbn, zenIsbn, price, new BigDecimal("4.95")));

        Sequence<Record> peopleAndBooksAndSalePrices = records.get(people).
                flatMap(outerJoin(records.get(books), using(isbn))).
                flatMap(outerJoin(records.get(prices), using(isbn)));

        Record dansFavouriteBook = peopleAndBooksAndSalePrices.filter(where(firstName, Grammar.is("dan"))).head();
        assertThat(dansFavouriteBook.get(firstName), Matchers.is("dan"));
        assertThat(dansFavouriteBook.get(title), Matchers.is("Zen And The Art Of Motorcycle Maintenance"));
        assertThat(dansFavouriteBook.get(price), matcher(between(new BigDecimal("4.95"), new BigDecimal("4.95"))));

        Record mattsFavouriteBook = peopleAndBooksAndSalePrices.filter(where(firstName, Grammar.is("matt"))).head();
        assertThat(mattsFavouriteBook.get(firstName), Matchers.is("matt"));
        assertThat(mattsFavouriteBook.get(title), Matchers.is("Godel, Escher, Bach: An Eternal Golden Braid"));
        assertThat(mattsFavouriteBook.get(price), Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void supportsUUID() throws Exception {
        Record record = records.get(books).filter(where(uuid, is(zenUuid))).head();
        assertThat(record.get(isbn), CoreMatchers.is(zenIsbn));
        assertThat(record.get(uuid), CoreMatchers.is(zenUuid));
    }

    @Test
    public void supportsBoolean() throws Exception {
        Record record = records.get(books).filter(where(inPrint, is(false))).head();
        assertThat(record.get(isbn), CoreMatchers.is(godelEsherBach));
        assertThat(record.get(inPrint), CoreMatchers.is(false));
    }

    @Test
    public void supportsBigDecimal() throws Exception {
        Record record = records.get(books).filter(where(rrp, greaterThan(new BigDecimal("20.00")))).head();
        assertThat(record.get(isbn), CoreMatchers.is(cleanCode));
        assertThat(record.get(rrp).setScale(2), CoreMatchers.is(new BigDecimal("34.99")));
    }

    @Test
    public void supportsUri() throws Exception {
        Record record = records.get(people).filter(where(isbn, is(zenIsbn))).head();
        assertThat(record.get(firstName), CoreMatchers.is("dan"));
        assertThat(record.get(isbn), CoreMatchers.is(zenIsbn));
    }

    @Test
    public void supportsIsNullAndNotNull() throws Exception {
        assertCount(records.add(people, record(firstName, "null age and dob", lastName, "", age, null, dob, null)), 1);
        assertThat(records.get(people).filter(where(age, is(notNullValue()))).toList().size(), NumberMatcher.is(3));
        assertThat(records.get(people).filter(not(where(age, is(nullValue())))).toList().size(), NumberMatcher.is(3));
        assertThat(records.get(people).filter(where(age, is(nullValue())).not()).toList().size(), NumberMatcher.is(3));
        Sequence<Record> recordSequence = records.get(people);
        assertThat(recordSequence.filter(where(age, is(nullValue()))).toList().size(), NumberMatcher.is(1));
    }

    @Test
    public void supportsReduce() throws Exception {
        assertThat(records.get(people).map(age).reduce(maximum(Integer.class)), CoreMatchers.is(12));
        assertThat(records.get(people).map(dob).reduce(minimum(Date.class)), CoreMatchers.is(date(1975, 1, 10)));
        assertThat(records.get(people).map(firstName).reduce(minimum(String.class)), CoreMatchers.is("bob"));
        assertThat(records.get(people).map(age).reduce(sum()), NumberMatcher.is(32));
        assertThat(records.get(people).map(age).reduce(average()).intValue(), NumberMatcher.is(10));

        assertThat(records.get(people).reduce(count()), NumberMatcher.is(3));
        records.add(people, record(firstName, "null age", lastName, "", age, null, dob, date(1974, 1, 10)));
        assertThat(records.get(people).map(age).reduce(count()), NumberMatcher.is(3));
    }

    @Test
    public void supportsReducingMultipleValuesAtTheSameTime() throws Exception {
        Record result = records.get(people).reduce(to(maximum(lastName), minimum(dob), sum(age), average(age), count(firstName)));
        assertThat(result.get(maximum(lastName)), CoreMatchers.is("savage"));
        assertThat(result.get(minimum(dob)), CoreMatchers.is(date(1975, 1, 10)));
        assertThat(result.get(sum(age)), NumberMatcher.is(32));
        assertThat(result.get(average(age)).intValue(), NumberMatcher.is(10));
        assertThat(result.get(count(firstName)), NumberMatcher.is(3));
    }

    @Test
    public void supportsAliasingAnAggregate() throws Exception {
        Record result = records.get(people).reduce(to(maximum(age).as(age)));
        assertThat(result.get(age), NumberMatcher.is(12));
    }

    @Test
    public void supportsSet() throws Exception {
        records.add(people, record(firstName, "chris", lastName, "bodart", age, 13, dob, date(1974, 1, 10)));
        assertThat(records.get(people).filter(where(lastName, startsWith("bod"))).map(select(lastName)).toSet(), hasExactly(record(lastName, "bodart")));
        assertThat(records.get(people).map(lastName).toSet(), containsInAnyOrder("bodart", "savage", "martin"));
    }

    @Test
    public void supportsUnique() throws Exception {
        records.add(people, record(firstName, "chris", lastName, "bodart", age, 13, dob, date(1974, 1, 10)));
        assertThat(records.get(people).filter(where(lastName, startsWith("bod"))).map(select(lastName)).unique(), hasExactly(record(lastName, "bodart")));
        assertThat(records.get(people).map(lastName).unique(), containsInAnyOrder("bodart", "savage", "martin"));
    }

    @Test
    public void supportsUpdating() throws Exception {
        assertCount(records.set(people, sequence(
                pair(where(age, is(12)), record(isbn, zenIsbn)),
                pair(where(age, is(11)), record(isbn, zenIsbn))
        )), 2);
        assertThat(records.get(people).filter(where(age, is(12))).map(isbn), hasExactly(zenIsbn));
        assertThat(records.get(people).filter(where(age, is(11))).map(isbn), hasExactly(zenIsbn));
    }

    @Test
    public void correctlyReportsCountWhenUpdatingValueUsedInPredicate() throws Exception {
        assertCount(records.set(people, sequence(
                pair(where(isbn, is(godelEsherBach)), record(isbn, zenIsbn)))), 1);
        assertThat(records.get(people).filter(where(age, is(12))).map(isbn), hasExactly(zenIsbn));
    }

    @Test
    public void supportsInsertOrUpdate() throws Exception {
        URI newIsbn = uri("urn:isbn:0192861980");
        String updatedTitle = "Zen And The Art Of Motorcycle Maintenance: 25th Anniversary Edition: An Inquiry into Values";
        String newTitle = "The Emperor's New Mind: Concerning Computers, Minds, and the Laws of Physics";
        assertCount(records.put(books,
                update(using(isbn),
                        record(isbn, zenIsbn, title, updatedTitle),
                        record(isbn, newIsbn, title, newTitle))
        ), 2);
        assertThat(records.get(books).filter(where(isbn, is(zenIsbn))).map(title), hasExactly(updatedTitle));
        assertThat(records.get(books).filter(where(isbn, is(newIsbn))).map(title), hasExactly(newTitle));
    }

    @Test
    public void doesNotPutExtraFields() throws Exception {
        assertCount(records.put(books, update(using(isbn),
                record(isbn, zenIsbn, firstName, "shouldBeIgnored"))
        ), 1);
        assertThat(records.get(books).filter(where(isbn, is(zenIsbn))).head().keywords().contains(firstName), CoreMatchers.is(false));
    }

    @Test
    public void doesNotSetExtraFields() throws Exception {
        assertCount(records.set(books, update(using(isbn),
                record(isbn, zenIsbn, firstName, "shouldBeIgnored"))
        ), 1);
        assertThat(records.get(books).filter(where(isbn, is(zenIsbn))).head().keywords().contains(firstName), CoreMatchers.is(false));
    }

    @Test
    public void supportsSelectingAllKeywords() throws Exception {
        assertThat(records.get(people).first().fields().size(), NumberMatcher.is(5));
        assertThat(records.get(definition(people.name(), age)).first().fields().size(), NumberMatcher.is(1));
    }

    @Test
    public void supportsMappingASingleKeyword() throws Exception {
        Sequence<String> names = records.get(people).map(firstName);
        assertThat(names, containsInAnyOrder("dan", "matt", "bob"));
    }

    @Test
    public void supportsAliasingTables() throws Exception {
        Record record = records.get(people.as("resources")).filter(where(lastName, is("bodart"))).map(select(firstName)).head();
        assertThat(record.get(firstName), Matchers.is("dan"));
    }

    @Test
    public void supportsAliasingAKeywordDuringSelection() throws Exception {
        Keyword<String> first = keyword("first", String.class);
        Record record = records.get(people).filter(where(lastName, is("bodart"))).map(select(firstName.as(first))).head();
        assertThat(record.get(first), Matchers.is("dan"));
        Keyword<String> result = Unchecked.cast(record.keywords().head());
        assertThat(result, Matchers.is(first));
    }

    @Test
    public void supportsAliasingAKeywordDuringFilter() throws Exception {
        Keyword<String> first = keyword("first", String.class);
        String last = records.get(people).filter(where(firstName.as(first), is("dan"))).map(lastName).head();
        assertThat(last, Matchers.is("bodart"));
    }

    @Test
    public void supportsSelectingMultipleKeywords() throws Exception {
        Sequence<Record> fullNames = records.get(people).map(select(firstName, lastName));
        assertThat(fullNames.first().fields().size(), NumberMatcher.is(2));
    }

    @Test
    public void supportsFilteringByASingleKeyword() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(11))).map(firstName);
        assertThat(names, hasExactly("bob"));
    }

    @Test
    public void supportsFilteringByMultipleKeywords() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(11)).and(where(lastName, is("martin")))).map(firstName);
        assertThat(names, hasExactly("bob"));
    }

    @Test
    public void supportsFilteringWithLogicalOr() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(12)).or(where(lastName, is("martin")))).map(firstName);
        assertThat(names, containsInAnyOrder("matt", "bob"));
    }

    @Test
    public void supportsFilteringWithLogicalOrCombineWithAnd() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(12)).and(where(lastName, is("savage"))).
                or(where(firstName, is("dan"))
                )).map(firstName);
        assertThat(names, containsInAnyOrder("dan", "matt"));
    }

    @Test
    public void supportsFilteringWithNot() throws Exception {
        assertThat(records.get(people).filter(where(firstName, is(not("bob")))).map(firstName), containsInAnyOrder("dan", "matt"));
        assertThat(records.get(people).filter(where(firstName, is("bob")).not()).map(firstName), containsInAnyOrder("dan", "matt"));
        assertThat(records.get(people).filter(not(where(firstName, is("bob")))).map(firstName), containsInAnyOrder("dan", "matt"));
    }

    @Test
    public void supportsFilteringWithGreaterThan() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(greaterThan(11)))).map(firstName);
        assertThat(names, hasExactly("matt"));
    }

    @Test
    public void supportsFilteringWithDates() throws Exception {
        assertThat(records.get(people).filter(where(dob, is(date(1977, 1, 10)))).map(firstName), containsInAnyOrder("dan"));
        assertThat(records.get(people).filter(where(dob, is(greaterThan(date(1977, 1, 1))))).map(firstName), containsInAnyOrder("dan"));
        assertThat(records.get(people).filter(where(dob, is(greaterThanOrEqualTo(date(1977, 1, 10))))).map(firstName), containsInAnyOrder("dan"));
        assertThat(records.get(people).filter(where(dob, is(lessThan(date(1976, 2, 10))))).map(firstName), containsInAnyOrder("matt", "bob"));
        assertThat(records.get(people).filter(where(dob, is(lessThanOrEqualTo(date(1975, 1, 10))))).map(firstName), containsInAnyOrder("matt"));
        assertThat(records.get(people).filter(where(dob, is(between(date(1975, 6, 10), date(1976, 6, 10))))).map(firstName), containsInAnyOrder("bob"));
    }

    @Test
    public void supportsFilteringWithStrings() throws Exception {
        assertThat(records.get(people).filter(where(firstName, is(greaterThan("e")))).map(firstName), containsInAnyOrder("matt"));
        assertThat(records.get(people).filter(where(firstName, is(greaterThanOrEqualTo("dan")))).map(firstName), containsInAnyOrder("dan", "matt"));
        assertThat(records.get(people).filter(where(firstName, is(lessThan("dan")))).map(firstName), containsInAnyOrder("bob"));
        assertThat(records.get(people).filter(where(firstName, is(lessThanOrEqualTo("dan")))).map(firstName), containsInAnyOrder("dan", "bob"));
        assertThat(records.get(people).filter(where(firstName, is(between("b", "d")))).map(firstName), containsInAnyOrder("bob"));
    }

    @Test
    public void supportsFilteringWithGreaterThanOrEqualTo() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(greaterThanOrEqualTo(11)))).map(firstName);
        assertThat(names, containsInAnyOrder("matt", "bob"));
    }

    @Test
    public void supportsFilteringWithLessThan() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(lessThan(12)))).map(firstName);
        assertThat(names, containsInAnyOrder("dan", "bob"));
    }

    @Test
    public void supportsFilteringWithLessThanOrEqualTo() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<String> names = users.filter(where(age, is(lessThanOrEqualTo(10)))).map(firstName);
        assertThat(names, containsInAnyOrder("dan"));
    }

    @Test
    public void supportsSorting() throws Exception {
        Sequence<Record> users = records.get(people).filter(where(age, is(notNullValue())));
        assertThat(users.sortBy(age).map(firstName), containsInAnyOrder("dan", "bob", "matt"));
        assertThat(users.sortBy(ascending(age)).map(firstName), containsInAnyOrder("dan", "bob", "matt"));
        assertThat(users.sortBy(descending(age)).map(firstName), containsInAnyOrder("matt", "bob", "dan"));
    }

    @Test
    public void supportsSize() throws Exception {
        Sequence<Record> users = records.get(people);
        assertThat(users.size(), NumberMatcher.is(3));
    }

    @Test
    public void supportsBetween() throws Exception {
        Sequence<Record> users = records.get(people);
        assertThat(users.filter(where(age, is(between(9, 11)))).map(firstName), containsInAnyOrder("dan", "bob"));
    }

    @Test
    public void supportsIn() throws Exception {
        Sequence<Record> users = records.get(people);
        assertThat(users.filter(where(age, is(in(9, 12)))).map(firstName), containsInAnyOrder("dan", "matt"));
    }

    @Test
    public void supportsInWithSubSelects() throws Exception {
        Sequence<Record> users = records.get(people);
        Sequence<Integer> ages = records.get(people).filter(where(firstName, is(between("a", "e")))).map(age);
        assertThat(users.filter(where(age, is(in(ages)))).map(firstName), containsInAnyOrder("dan", "bob"));
    }

    @Test
    public void supportsStartsWith() throws Exception {
        assertThat(records.get(people).filter(where(firstName, startsWith("d"))).map(firstName), hasExactly("dan"));
    }

    @Test
    public void supportsContains() throws Exception {
        Sequence<Record> users = records.get(people);
        assertThat(users.filter(where(firstName, contains("a"))).map(firstName), containsInAnyOrder("dan", "matt"));
    }

    @Test
    public void supportsEndsWith() throws Exception {
        Sequence<Record> users = records.get(people);
        assertThat(users.filter(where(firstName, endsWith("n"))).map(firstName), hasExactly("dan"));
    }

    @Test
    public void supportsRemove() throws Exception {
        assertCount(records.remove(people, where(age, is(greaterThan(10)))), 2);
        assertThat(records.get(people).size(), equalTo(1));
        assertCount(records.remove(people, where(age, is(greaterThan(10)))), 0);

        assertThat(records.get(people).size(), equalTo(1));

        assertCount(records.remove(people), 1);
        assertThat(records.get(people).size(), equalTo(0));
    }

    @Test
    public void willNotFailIfAskedToAddAnEmptySequenceOfRecords() throws Exception {
        assertThat(records.add(people, new Record[0]), equalTo(0));
        assertThat(records.add(people, Sequences.<Record>sequence()), equalTo(0));
    }

    @Test
    public void closesResourcesEvenIfYouDontIterateToTheEnd() throws IOException {
        if (records instanceof Closeable) {
            Closeable closeable = (Closeable) records;
            closeable.close();
            Iterator<Record> results = records.get(people).iterator();
            assertThat(results.next(), Matchers.is(Matchers.notNullValue()));
            closeable.close();
            try {
                results.next();
                fail("We should get some kind of already closed exception");
            } catch (Exception e) {
                // all good
            }
        }
    }

    @Test
    public void fieldsOfRecordAddedAndRetrievedInSameOrder() throws Exception {
        Sequence<Record> dan = records.get(people).filter(where(firstName, is("dan")));
        Assert.assertThat(dan.head().fields().map(Callables.<Keyword<?>>first()), IterableMatcher.<Keyword<?>>hasExactly(people.fields()));
    }

    @Test
    public void supportsDrop() throws Exception {
        assertThat(records.get(people).sortBy(firstName).drop(2).size(), NumberMatcher.is(1));
        assertThat(records.get(people).sortBy(firstName).drop(2).map(firstName), hasExactly("matt"));
    }

    @Test
    public void supportsTake() throws Exception {
        assertThat(records.get(people).sortBy(firstName).take(2).size(), NumberMatcher.is(2));
        assertThat(records.get(people).sortBy(firstName).take(1).map(firstName), hasExactly("bob"));
    }

    @Test
    public void canCombineDropAndTake() throws Exception {
        assertThat(records.get(people).sortBy(firstName).drop(1).take(1).size(), NumberMatcher.is(1));
        assertThat(records.get(people).sortBy(firstName).take(2).drop(1).size(), NumberMatcher.is(1));
        assertThat(records.get(people).sortBy(firstName).drop(1).take(2).drop(1).map(firstName), hasExactly("matt"));
    }

    @Test
    public void supportsExists() throws Exception {
        assertThat(records.get(people).exists(where(firstName, is("dan"))), CoreMatchers.is(true));
        assertThat(records.get(people).exists(where(firstName, is("george bush"))), CoreMatchers.is(false));
    }

    @Test
    public void supportsFind() throws Exception {
        assertThat(records.get(people).find(where(firstName, is("dan"))).isEmpty(), CoreMatchers.is(false));
    }

    @Test
    public void supportsConcatenationDuringSelection() throws Exception {
        Keyword<String> fullName = concat(firstName, lastName);
        assertThat(records.get(people).sortBy(age).map(fullName).head(), CoreMatchers.is("danbodart"));
        assertThat(records.get(people).sortBy(age).map(select(fullName)).head().get(fullName), CoreMatchers.is("danbodart"));
    }

    @Test
    public void supportsConcatenationDuringFiltering() throws Exception {
        Keyword<String> fullName = concat(firstName, lastName);
        assertThat(records.get(people).filter(where(fullName, is("danbodart"))).map(age).head(), CoreMatchers.is(9));
    }

    @Test
    public void canFullyQualifyAKeywordDuringSelection() throws Exception {
        AliasedKeyword<String> fullyQualified = firstName.of(people);
        assertThat(records.get(people).filter(where(age, is(9))).map(select(fullyQualified)).head().get(fullyQualified), CoreMatchers.is("dan"));
    }

    @Test
    public void canFullyQualifyAKeywordDuringFiltering() throws Exception {
        AliasedKeyword<String> fullyQualified = firstName.of(people);
        assertThat(records.get(people).filter(where(fullyQualified, is("dan"))).map(age).head(), CoreMatchers.is(9));
    }

    @Test
    public void putDoesntRemoveOtherFields() throws Exception {
        Keyword<String> aka = keyword("aka", String.class);
        records.put(definition(books.name(), isbn, aka), update(using(isbn), record(isbn, zenIsbn, aka, "The Meaning of Life")));
        Record record = records.get(definition(books.name(), books.fields().cons(aka))).find(Predicates.where(People.isbn, Predicates.is(zenIsbn))).get();
        assertThat(record.fields().size(), com.googlecode.totallylazy.matchers.Matchers.is(books.fields().size() + 1));
    }
}
