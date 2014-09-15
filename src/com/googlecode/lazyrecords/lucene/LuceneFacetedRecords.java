package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Facet;
import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.search.Query;

import java.io.IOException;

import static com.googlecode.lazyrecords.Facet.FacetEntry;
import static com.googlecode.totallylazy.Sequences.sequence;

public class LuceneFacetedRecords implements FacetedRecords {

    private final FacetedLuceneStorage facetedLuceneStorage;
    private final Lucene lucene;
    private final LuceneQueryPreprocessor luceneQueryPreprocessor;

    public LuceneFacetedRecords(FacetedLuceneStorage facetedLuceneStorage, LuceneMappings mappings, LuceneQueryPreprocessor luceneQueryPreprocessor) {
        this.facetedLuceneStorage = facetedLuceneStorage;
        this.lucene = new Lucene(mappings.stringMappings());
        this.luceneQueryPreprocessor = luceneQueryPreprocessor;
    }

    @Override
    public <T extends Pair<Keyword<?>, Integer>> Sequence<Facet<FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests) throws IOException {
        final Searcher searcher = facetedLuceneStorage.searcher();
        final TaxonomyReader taxonomyReader = facetedLuceneStorage.taxonomyReader();
        final FacetsCollector facetsCollector = new FacetsCollector();
        final Query query = lucene.query(predicate);
        final Query processedQuery = new LuceneQueryVisitor(luceneQueryPreprocessor).visit(query);
        searcher.search(processedQuery, facetsCollector);

        final Facets facets = new FastTaxonomyFacetCounts(taxonomyReader, new FacetsConfig(), facetsCollector);
        return facetsRequests.map(new Mapper<Pair<Keyword<?>, Integer>, Facet<FacetEntry>>() {
            @Override
            public Facet<FacetEntry> call(Pair<Keyword<?>, Integer> facetKeyword) throws Exception {
                final FacetResult facetResult = facets.getTopChildren(facetKeyword.second(), facetKeyword.first().name());
                final Sequence<FacetEntry> facetsAndCounts = (facetResult == null) ? Sequences.<FacetEntry>empty() : sequence(facetResult.labelValues).map(toLabelAndCountPair());
                return Facet.facet(facetKeyword.first(), facetsAndCounts);
            }
        });
    }

    private Mapper<LabelAndValue, FacetEntry> toLabelAndCountPair() {
        return new Mapper<LabelAndValue, FacetEntry>() {
            @Override
            public FacetEntry call(LabelAndValue labelAndValue) throws Exception {
                return FacetEntry.facetEntry(labelAndValue.label, labelAndValue.value);
            }
        };
    }
}
