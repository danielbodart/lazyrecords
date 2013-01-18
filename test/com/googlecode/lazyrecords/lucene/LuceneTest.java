package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.ImmutableKeyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.BooleanMapping;
import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.predicates.AndPredicate;
import com.googlecode.totallylazy.predicates.EqualsPredicate;
import com.googlecode.totallylazy.predicates.LogicalPredicate;
import com.googlecode.totallylazy.predicates.WherePredicate;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.totallylazy.predicates.AndPredicate.and;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LuceneTest {

    public static final ImmutableKeyword<String> Z = Keywords.keyword("zzz", String.class);
    public static final ImmutableKeyword<String> Y = Keywords.keyword("yyy", String.class);
    public static final ImmutableKeyword<String> X = Keywords.keyword("xxx", String.class);
    public static final Definition DEFINITION = Definition.constructors.definition("testy", X, Y, Z);

    private final LogicalPredicate<Record> whereXEqualsX = whereXIsTrue();
    private final LogicalPredicate<Record> whereYEqualsY = whereYIsNull();
    private final LogicalPredicate<Record> whereZEqualsZ = whereZIsNull();
    private Lucene lucene;

    @Before
    public void setup() {
        StringMappings stringMappings = new StringMappings();
        StringMapping<Boolean> booleanMapper = new BooleanMapping();
        stringMappings.add(Boolean.class, booleanMapper);

        lucene = new Lucene(stringMappings);
    }

    @Test
    public void shouldSeeRecord_with_XTrueYNullZNull() throws Exception {
        LuceneRecords records = createRecords();
        records.add(DEFINITION, Record.constructors.record(X, "true"));
        Sequence<Record> results = doQuery(records);
        assertThat(results.size(), is(1));
    }

    private LuceneRecords createRecords() throws IOException {
        Directory directory   = new RAMDirectory();
        return new LuceneRecords(new OptimisedStorage(directory, new LucenePool(directory)));
    }

    @Test
    public void shouldSeeRecord_with_XFalseYNotNullZNull() throws Exception {
        LuceneRecords records = createRecords();
        records.add(DEFINITION, Record.constructors.record(X, "false", Y, "not null"));
        Sequence<Record> results = doQuery(records);
        assertThat(results.size(), is(0));
    }

    @Test
    public void shouldSeeRecord_with_XFalseYNullZNull() throws Exception {
        LuceneRecords records = createRecords();
        records.add(DEFINITION, Record.constructors.record(X, "false"));
        Sequence<Record> results = doQuery(records);
        assertThat(results.size(), is(1));
    }

    @Test
    public void shouldSeeRecord_with_XFalseYNullZNull_with_explicitXFalseInSubQuery() throws Exception {
        LuceneRecords records = createRecords();
        records.add(DEFINITION, Record.constructors.record(X, "false"));
        Sequence<Record> results = doQuery(records, createQueryXOr_YIsNullAndZIsNullAndNotX());
        assertThat(results.size(), is(1));
    }

    @Test
    public void shouldSeeRecord_with_XTrueYNotNullZNotNull() throws Exception {
        LuceneRecords records = createRecords();
        records.add(DEFINITION, Record.constructors.record(X, "true", Y, "something", Z, "something"));
        Sequence<Record> results = doQuery(records);
        assertThat(results.size(), is(1));
    }

    @Test
    public void shouldSeeRecord_with_HandcraftedHQL() throws Exception {
        LuceneRecords records = createRecords();
        records.add(DEFINITION, Record.constructors.record(X, "false"));
        String queryString =
                " ( ( *:* -yyy:[* TO *] ) AND ( *:* -zzz:[* TO *] ) ) OR xxx:true";
        System.out.println(queryString);
        Query parsedQuery = new QueryParser(Version.LUCENE_36, "default field for query term", new StandardAnalyzer(Version.LUCENE_36)).parse(queryString);
        Sequence<Record> otherResults = records.query(parsedQuery, DEFINITION.fields());
        assertThat(otherResults.size(), is(1));
    }

    private Sequence<Record> doQuery(LuceneRecords records) {
        Query luceneQuery = createQueryXOr_YIsNullAndZIsNull();
        return doQuery(records, luceneQuery);
    }

    private Sequence<Record> doQuery(LuceneRecords records, Query luceneQuery) {
        return records.query(luceneQuery, DEFINITION.fields());
    }

    private Query createQueryXOr_YIsNullAndZIsNullAndNotX() {
        Iterable<LogicalPredicate<Record>>  yAndZPredicates = Sequences.<LogicalPredicate<Record>>sequence().add(whereYEqualsY).add(whereZEqualsZ);
        LogicalPredicate<Record>            zAndYNotX       = AndPredicate.<Record>and(yAndZPredicates).and(whereXIsFalse());

        Query luceneQuery = lucene.query(whereXEqualsX.or(zAndYNotX));

        logHACK((BooleanQuery)luceneQuery);

        return luceneQuery;
    }

    private Query createQueryXOr_YIsNullAndZIsNull() {
        Iterable<LogicalPredicate<Record>>  yAndZPredicates = Sequences.<LogicalPredicate<Record>>sequence().add(whereYEqualsY).add(whereZEqualsZ);
        LogicalPredicate<Record>            zAndY           = and(yAndZPredicates);

        Query luceneQuery = lucene.query(whereXEqualsX.or(zAndY));

        logHACK((BooleanQuery)luceneQuery);

        return luceneQuery;
    }

    private void logHACK(BooleanQuery luceneQuery) {
        System.out.println(luceneQuery + "\n");
        BooleanClause[] clauses = luceneQuery.getClauses();
        int i = 1;
        for (BooleanClause aClause : clauses) {
            System.out.println(i++ + ": " + aClause);
        }
    }

    private LogicalPredicate<Record> whereZIsNull() {
        LogicalPredicate<String> zEqualsZ = Predicates.nullValue();;
        Callable1<Record, String> keywordZ = Z;
        return WherePredicate.where(keywordZ, zEqualsZ);
    }

    private LogicalPredicate<Record> whereYIsNull() {
        LogicalPredicate<String> yEqualsY = Predicates.nullValue();
        Callable1<Record, String> keywordY = Y;
        return WherePredicate.<Record, String>where(keywordY, yEqualsY);
    }

    private LogicalPredicate<Record> whereXIsTrue() {
        LogicalPredicate<String> xEqualsX = EqualsPredicate.<String>equalTo("true");
        Callable1<Record, String> keywordX = X;
        return WherePredicate.<Record, String>where(keywordX, xEqualsX);
    }

    private LogicalPredicate<Record> whereXIsFalse() {
        LogicalPredicate<String> xEqualsX = EqualsPredicate.<String>equalTo("false");
        Callable1<Record, String> keywordX = X;
        return WherePredicate.<Record, String>where(keywordX, xEqualsX);
    }


}
