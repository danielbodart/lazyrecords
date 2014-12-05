package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.lazyrecords.Facet.FacetEntry;
import static com.googlecode.lazyrecords.Facet.FacetEntry.facetEntry;
import static com.googlecode.lazyrecords.FacetDrillDown.facetDrillDown;
import static com.googlecode.lazyrecords.FacetRequest.facetRequest;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.lucene.PartitionedIndex.methods.indexWriter;
import static com.googlecode.totallylazy.Predicates.*;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class LuceneFacetedRecordsTest {

    private FacetedRecords facetedRecords;
    private TaxonomyFacetedLuceneStorage luceneFacetedStorage;

    @Before
    public void setUp() throws Exception {
        final Directory taxonomyDirectory = new RAMDirectory();
        luceneFacetedStorage = new TaxonomyFacetedLuceneStorage(testStorage(), taxonomyDirectory, new FacetsConfig());
        facetedRecords = new LuceneFacetedRecords(luceneFacetedStorage, new LuceneMappings(), new DoNothingLuceneQueryPreprocessor());

        luceneFacetedStorage.add(testDocuments());
        luceneFacetedStorage.flush();
    }

    private Sequence<Document> testDocuments() {
        final Document doc1 = new Document();
        doc1.add(new TextField("Author", "Mark Twain", Field.Store.YES));
        doc1.add(new TextField("Year", "2004", Field.Store.YES));

        final Document doc2 = new Document();
        doc2.add(new TextField("Author", "Mark Twain", Field.Store.YES));
        doc2.add(new TextField("Year", "2005", Field.Store.YES));
        doc2.add(new TextField("Publisher", "Bloomsbury", Field.Store.YES));

        final Document doc3 = new Document();
        doc3.add(new TextField("Author", "Charles Dickens", Field.Store.YES));
        doc3.add(new TextField("Year", "2004", Field.Store.YES));

        return sequence(doc1, doc2, doc3);
    }

    @Test
    public void shouldReturnAFacetWithNoEntriesIfThereAreNoEntriesForThatFacet() throws Exception {
        final Keyword<?> facetKeyword = keyword("Facet");
        final FacetRequest facetRequest = facetRequest(facetKeyword);
        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), one(facetRequest));

        final Facet<FacetEntry> expectedFacet = facetResults.head();
        assertThat(expectedFacet.key(), Matchers.<Keyword<?>>is(facetKeyword));
        assertThat(expectedFacet.size(), Matchers.is(0));
    }

    @Test
    public void shouldReturnTheExpectedFacetsSortedByCount() throws Exception {
        final Keyword<?> authorKeyword = keyword("Author");
        final FacetRequest authorRequest = facetRequest(authorKeyword);

        final Keyword<?> yearKeyword = keyword("Year");
        final FacetRequest yearRequest = facetRequest(yearKeyword);

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(authorRequest, yearRequest));

        final Facet<FacetEntry> authorFacet = facetResults.first();
        assertThat(authorFacet.key(), Matchers.<Keyword<?>>is(authorKeyword));
        assertThat(authorFacet, Matchers.contains(facetEntry("Mark Twain", 2), facetEntry("Charles Dickens", 1)));

        final Facet<FacetEntry> yearFacet = facetResults.second();
        assertThat(yearFacet.key(), Matchers.<Keyword<?>>is(yearKeyword));
        assertThat(yearFacet, Matchers.contains(facetEntry("2004", 2), facetEntry("2005", 1)));
    }

    @Test
    public void shouldReturnOnlyTheSpecifiedNumberOfFacets() throws Exception {

        final Keyword<?> yearKeyword = keyword("Year");
        final FacetRequest yearRequest = facetRequest(yearKeyword, 1);

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(yearRequest));

        final Facet<FacetEntry> yearFacet = facetResults.first();
        assertThat(yearFacet.key(), Matchers.<Keyword<?>>is(yearKeyword));
        assertThat(yearFacet, Matchers.contains(facetEntry("2004", 2)));
    }

    @Test
    public void shouldUpdateTheFacets() throws Exception {
        luceneFacetedStorage.delete(new TermQuery(new Term("Author", "Charles Dickens")));
        luceneFacetedStorage.flush();

        final Keyword<?> yearKeyword = keyword("Year");
        final FacetRequest yearRequest = facetRequest(yearKeyword);

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(yearRequest));

        final Facet<FacetEntry> yearFacet = facetResults.first();
        assertThat(yearFacet.key(), Matchers.<Keyword<?>>is(yearKeyword));
        assertThat(yearFacet, containsInAnyOrder(facetEntry("2004", 1), facetEntry("2005", 1)));
    }

    @Test
    public void shouldReturnTheExpectedFacetsForAFieldThatIsMissingInSomeDocuments() throws Exception {
        final Keyword<?> publisherKeyword = keyword("Publisher");
        final FacetRequest publisherRequest = facetRequest(publisherKeyword);

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(publisherRequest));

        final Facet<FacetEntry> authorFacet = facetResults.first();
        assertThat(authorFacet.key(), Matchers.<Keyword<?>>is(publisherKeyword));
        assertThat(authorFacet, Matchers.contains(facetEntry("Bloomsbury", 1)));
    }

    @Test
    public void shouldReturnTheExpectedFacetsAccordingToAQuery() throws Exception {
        final Keyword<?> authorKeyword = keyword("Author");
        final FacetRequest authorRequest = facetRequest(authorKeyword);

        final Keyword<?> yearKeyword = keyword("Year");
        final FacetRequest yearRequest = facetRequest(yearKeyword);

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(where(keyword("Year", String.class), is("2004")), sequence(authorRequest, yearRequest));

        final Facet<FacetEntry> authorFacet = facetResults.first();
        assertThat(authorFacet.key(), Matchers.<Keyword<?>>is(authorKeyword));
        assertThat(authorFacet, containsInAnyOrder(facetEntry("Mark Twain", 1), facetEntry("Charles Dickens", 1)));

        final Facet<FacetEntry> yearFacet = facetResults.second();
        assertThat(yearFacet.key(), Matchers.<Keyword<?>>is(yearKeyword));
        assertThat(yearFacet, Matchers.contains(facetEntry("2004", 2)));
    }

    @Test
    public void shouldReturnFacetEntriesWithTheCorrectType() throws Exception {

        final Document doc = new Document();
        doc.add(new TextField("Published", "true", Field.Store.YES));
        luceneFacetedStorage.add(one(doc));
        luceneFacetedStorage.flush();

        final Keyword<?> publishedKeyword = keyword("Published", Boolean.class);
        final FacetRequest publishedRequest = facetRequest(publishedKeyword);

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(publishedRequest));

        final Facet<FacetEntry> facet = facetResults.first();
        assertThat(facet.key(), Matchers.<Keyword<?>>is(publishedKeyword));
        assertThat(facet, Matchers.contains(facetEntry(true, 1)));
    }

    @Test
    public void shouldSupportDrillingSideways() throws Exception {
        final Keyword<?> authorKeyword = keyword("Author");
        final FacetRequest authorRequest = facetRequest(authorKeyword);

        final Keyword<?> yearKeyword = keyword("Year");
        final FacetRequest yearRequest = facetRequest(yearKeyword);

        final FacetDrillDown authorDrillDown = facetDrillDown(authorKeyword, sequence("Mark Twain"));

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(authorRequest, yearRequest), one(authorDrillDown));

        final Facet<FacetEntry> authorFacet = facetResults.first();
        assertThat(authorFacet.key(), Matchers.<Keyword<?>>is(authorKeyword));
        assertThat(authorFacet, Matchers.contains(facetEntry("Mark Twain", 2), facetEntry("Charles Dickens", 1)));

        final Facet<FacetEntry> yearFacet = facetResults.second();
        assertThat(yearFacet.key(), Matchers.<Keyword<?>>is(yearKeyword));
        assertThat(yearFacet, containsInAnyOrder(facetEntry("2004", 1), facetEntry("2005", 1)));
    }

    @Test
    public void shouldSupportDrillingSidewaysOnMultipleFacets() throws Exception {
        final Keyword<?> authorKeyword = keyword("Author");
        final FacetRequest authorRequest = facetRequest(authorKeyword);

        final Keyword<?> yearKeyword = keyword("Year");
        final FacetRequest yearRequest = facetRequest(yearKeyword);

        final FacetDrillDown authorDrillDown = facetDrillDown(authorKeyword, sequence("Mark Twain"));
        final FacetDrillDown yearDrillDown = facetDrillDown(yearKeyword, sequence("2004"));

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(authorRequest, yearRequest), sequence(authorDrillDown, yearDrillDown));

        final Facet<FacetEntry> authorFacet = facetResults.first();
        assertThat(authorFacet.key(), Matchers.<Keyword<?>>is(authorKeyword));
        assertThat(authorFacet, Matchers.contains(facetEntry("Mark Twain", 1), facetEntry("Charles Dickens", 1)));

        final Facet<FacetEntry> yearFacet = facetResults.second();
        assertThat(yearFacet.key(), Matchers.<Keyword<?>>is(yearKeyword));
        assertThat(yearFacet, containsInAnyOrder(facetEntry("2004", 1), facetEntry("2005", 1)));
    }

    @Test
    public void shouldSupportDrillingSidewaysWithMultipleValuesOnOneFacet() throws Exception {
        final Keyword<?> authorKeyword = keyword("Author");
        final FacetRequest authorRequest = facetRequest(authorKeyword);

        final Keyword<?> yearKeyword = keyword("Year");
        final FacetRequest yearRequest = facetRequest(yearKeyword);

        final FacetDrillDown authorDrillDown = facetDrillDown(authorKeyword, sequence("Mark Twain", "Charles Dickens"));

        final Sequence<Facet<FacetEntry>> facetResults = facetedRecords.facetResults(alwaysTrue(Record.class), sequence(authorRequest, yearRequest), one(authorDrillDown));

        final Facet<FacetEntry> authorFacet = facetResults.first();
        assertThat(authorFacet.key(), Matchers.<Keyword<?>>is(authorKeyword));
        assertThat(authorFacet, Matchers.contains(facetEntry("Mark Twain", 2), facetEntry("Charles Dickens", 1)));

        final Facet<FacetEntry> yearFacet = facetResults.second();
        assertThat(yearFacet.key(), Matchers.<Keyword<?>>is(yearKeyword));
        assertThat(yearFacet, containsInAnyOrder(facetEntry("2004", 2), facetEntry("2005", 1)));
    }

    private LuceneStorage testStorage() throws IOException {
        return new OptimisedStorage(indexWriter(new RAMDirectory()));
    }
}