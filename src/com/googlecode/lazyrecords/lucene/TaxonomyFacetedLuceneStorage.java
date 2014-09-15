package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.lucene.mappings.DelegatingStorage;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Callers;
import com.googlecode.totallylazy.Closeables;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.UnaryFunction;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;

import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.sequence;

public class TaxonomyFacetedLuceneStorage extends DelegatingStorage implements FacetedLuceneStorage {

    private final Lazy<DirectoryTaxonomyWriter> lazyTaxonomyWriter;
    private final FacetsConfig facetsConfig;


    public TaxonomyFacetedLuceneStorage(LuceneStorage storage, final Directory directory, FacetsConfig facetsConfig) {
        super(storage);
        this.lazyTaxonomyWriter = new Lazy<DirectoryTaxonomyWriter>() {
            @Override
            protected DirectoryTaxonomyWriter get() throws Exception {
                return new DirectoryTaxonomyWriter(directory);
            }
        };
        this.facetsConfig = facetsConfig;
    }

    @Override
    public Number add(Sequence<Document> documents) throws IOException {
        final Sequence<Document> facetedDocuments = documents.map(new UnaryFunction<Document>() {
            @Override
            public Document call(Document document) throws Exception {
                final Document facetedDocument = sequence(document.getFields()).fold(new Document(), withFacetFields());
                final DirectoryTaxonomyWriter taxonomyWriter = Callers.call(lazyTaxonomyWriter);
                return facetsConfig.build(taxonomyWriter, facetedDocument);
            }
        }).realise();

        return storage.add(facetedDocuments);
    }

    @Override
    public void flush() throws IOException {
        Callers.call(lazyTaxonomyWriter).commit();
        storage.flush();
    }

    @Override
    public void close() throws IOException {
        Closeables.close(Callers.call(lazyTaxonomyWriter));
        storage.close();
    }

    private Callable2<Document, IndexableField, Document> withFacetFields() {
        return new Callable2<Document, IndexableField, Document>() {
            @Override
            public Document call(Document document, IndexableField indexableField) throws Exception {
                if (!indexableField.stringValue().isEmpty())
                    document.add(new FacetField(indexableField.name(), indexableField.stringValue()));
                document.add(indexableField);
                return document;
            }
        };
    }

    @Override
    public TaxonomyReader taxonomyReader() throws IOException {
        return new DirectoryTaxonomyReader(Callers.call(lazyTaxonomyWriter));
    }
}
