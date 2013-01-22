package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.matchers.IterableMatcher;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import static com.googlecode.lazyrecords.Grammar.*;
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
    protected static Keyword<Integer> age = keyword("age", Integer.class);
    protected static Keyword<Date> dob = keyword("dob", Date.class);
    protected static ImmutableKeyword<String> firstName = keyword("firstName", String.class);
    protected static Keyword<String> lastName = keyword("lastName", String.class);

    protected static Keyword<URI> isbn = keyword("isbn", URI.class);
    protected static Keyword<String> title = keyword("title", String.class);
    protected static Keyword<Boolean> inPrint = keyword("inPrint", Boolean.class);
    protected static Keyword<UUID> uuid = keyword("uuid", UUID.class);
    protected static Keyword<BigDecimal> rrp = keyword("rrp", BigDecimal.class);

    protected static Definition people = definition("people", isbn, age, dob, firstName, lastName);
    protected static Definition books = definition("books", isbn, title, inPrint, uuid, rrp);

    public static final URI zenIsbn = uri("urn:isbn:0099322617");
    public static final URI godelEsherBach = uri("urn:isbn:0140289208");
    public static final URI cleanCode = uri("urn:isbn:0132350882");
    public static final UUID zenUuid = randomUUID();

    protected T records;

    protected Logger logger;
    private ByteArrayOutputStream stream;
    protected boolean supportsRowCount = true;

    protected abstract T createRecords() throws Exception;

    @Before
    public void setupRecords() throws Exception {
        stream = new ByteArrayOutputStream();
        logger = new PrintStreamLogger(new PrintStream(streams(System.out, stream)));
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
    public void supportsJoinUsing() throws Exception {
        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(join(records.get(books), using(isbn))).
                head().fields().size(), NumberMatcher.is(9));

        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(join(records.get(books), using(isbn))).
                map(select(firstName, isbn)).
                head().fields().size(), NumberMatcher.is(2));

        assertThat(records.get(people).map(select(isbn, age)).filter(where(age, is(lessThan(12)))).
                flatMap(join(records.get(books).map(select(title, isbn)), using(isbn))).
                head().fields().size(), NumberMatcher.is(3));
    }

    @Test
    public void supportsJoinOn() throws Exception {
        Keyword<BigDecimal> price = keyword("price", BigDecimal.class);
        Keyword<URI> book = keyword("book", URI.class);
        Definition prices = definition("prices", book, price);

        records.remove(prices);
        records.add(prices, record(book, zenIsbn, price, new BigDecimal("4.95")));

        Sequence<Record> peopleAndSalePrices = records.get(people).
                flatMap(leftJoin(records.get(prices), on(isbn, book)));

        Record dansFavouriteBook = peopleAndSalePrices.filter(where(firstName, is("dan"))).head();
        assertThat(dansFavouriteBook.get(firstName), Matchers.is("dan"));
        assertThat(dansFavouriteBook.get(price), matcher(between(new BigDecimal("4.95"), new BigDecimal("4.95"))));

        Record mattsFavouriteBook = peopleAndSalePrices.filter(where(firstName, is("matt"))).head();
        assertThat(mattsFavouriteBook.get(firstName), Matchers.is("matt"));
        assertThat(mattsFavouriteBook.get(price), Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void supportsLeftJoinUsing() throws Exception {
        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(leftJoin(records.get(books), using(isbn))).
                head().fields().size(), NumberMatcher.is(9));

        assertThat(records.get(people).filter(where(age, is(lessThan(12)))).
                flatMap(leftJoin(records.get(books), using(isbn))).
                map(select(firstName, isbn)).
                head().fields().size(), NumberMatcher.is(2));

        assertThat(records.get(people).map(select(isbn, age)).filter(where(age, is(lessThan(12)))).
                flatMap(leftJoin(records.get(books).map(select(title, isbn)), using(isbn))).
                head().fields().size(), NumberMatcher.is(3));

        Record personWithNoCorrespondingBook = record(firstName, "ray", lastName, "barlow", age, 9, dob, date(1977, 1, 10), isbn, uri("urn:isbn:0000000000"));
        records.remove(people);
        records.add(people, personWithNoCorrespondingBook);

        assertThat(records.get(people).map(select(isbn, firstName)).filter(where(firstName, is("ray"))).
                flatMap(leftJoin(records.get(books).map(select(title, isbn)), using(isbn))).
                head().get(firstName), Matchers.is("ray"));
    }

    @Test
    public void supportsJoiningAcrossMoreThanTwoTables() {
        Keyword<BigDecimal> salePrice = keyword("salePrice", BigDecimal.class);
        Definition salePrices = Grammar.definition("salePrices", isbn, salePrice);
        records.remove(salePrices);
        records.add(salePrices, record(isbn, zenIsbn, salePrice, new BigDecimal("4.95")));

        Sequence<Record> peopleAndBooksAndSalePrices = records.get(people).
                flatMap(leftJoin(records.get(books), using(isbn))).
                flatMap(leftJoin(records.get(salePrices), using(isbn)));

        Record dansFavouriteBook = peopleAndBooksAndSalePrices.filter(where(firstName, Grammar.is("dan"))).head();
        assertThat(dansFavouriteBook.get(firstName), Matchers.is("dan"));
        assertThat(dansFavouriteBook.get(title), Matchers.is("Zen And The Art Of Motorcycle Maintenance"));
        assertThat(dansFavouriteBook.get(salePrice), matcher(between(new BigDecimal("4.95"), new BigDecimal("4.95"))));

        Record mattsFavouriteBook = peopleAndBooksAndSalePrices.filter(where(firstName, Grammar.is("matt"))).head();
        assertThat(mattsFavouriteBook.get(firstName), Matchers.is("matt"));
        assertThat(mattsFavouriteBook.get(title), Matchers.is("Godel, Escher, Bach: An Eternal Golden Braid"));
        assertThat(mattsFavouriteBook.get(salePrice), Matchers.is(Matchers.nullValue()));
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
}
