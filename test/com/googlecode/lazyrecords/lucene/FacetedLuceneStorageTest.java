package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Predicates;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.methods.indexWriter;
import static com.googlecode.totallylazy.Sequences.one;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FacetedLuceneStorageTest {
    private LuceneStorage delegatedStorage;
    private FacetedLuceneStorage storage;
    private Directory taxonomyDirectory;

    @Before
    public void setupStorage() throws Exception {
        taxonomyDirectory = new RAMDirectory();
        delegatedStorage = testStorage();
        storage = new TaxonomyFacetedLuceneStorage(delegatedStorage, taxonomyDirectory, new FacetsConfig(), new FieldBasedFacetingPolicy(Predicates.is("name")));
    }

    @After
    public void closeStorage() throws Exception {
        storage.close();
    }

    @Test
    public void shouldPopulateBothTheTaxonomyIndexAndTheDecoratedIndexWhenAddingDocuments() throws Exception {

        final int originalSize = storage.taxonomyReader().getSize();

        final Document document = new Document();
        document.add(new TextField("name", "value", Field.Store.YES));
        storage.add(one(document));
        storage.flush();

        assertThat(storage.taxonomyReader().getSize(), is(originalSize + 2));
        assertThat(delegatedStorage.count(new MatchAllDocsQuery()), is(1));
    }

    @Test
    public void shouldNotGenerateTaxonomyInformationForFieldsWithEmptyValue() throws Exception {
        final int originalSize = storage.taxonomyReader().getSize();

        final Document document = new Document();
        document.add(new TextField("name", "", Field.Store.YES));
        storage.add(one(document));
        storage.flush();

        assertThat(storage.taxonomyReader().getSize(), is(originalSize));
        assertThat(delegatedStorage.count(new MatchAllDocsQuery()), is(1));
    }

    @Test
    public void shouldNotGenerateTaxonomyInformationForFieldsThatShouldNotBeFaceted() throws Exception {
        final int originalSize = storage.taxonomyReader().getSize();

        final Document document = new Document();
        document.add(new TextField("non-faceted-field", "value", Field.Store.YES));
        storage.add(one(document));
        storage.flush();

        assertThat(storage.taxonomyReader().getSize(), is(originalSize));
        assertThat(delegatedStorage.count(new MatchAllDocsQuery()), is(1));
    }

    private LuceneStorage testStorage() throws IOException {
        return new OptimisedStorage(indexWriter(new RAMDirectory()));
    }
}