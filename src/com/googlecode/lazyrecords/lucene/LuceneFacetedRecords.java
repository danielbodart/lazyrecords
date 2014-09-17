package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Facet;
import com.googlecode.lazyrecords.FacetedRecords;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Group;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;

import static com.googlecode.lazyrecords.Facet.FacetEntry;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.apache.lucene.facet.DrillSideways.DrillSidewaysResult;

public class LuceneFacetedRecords implements FacetedRecords {

    private final FacetedLuceneStorage facetedLuceneStorage;
    private final Lucene lucene;
    private final LuceneQueryPreprocessor luceneQueryPreprocessor;
    private final StringMappings stringMappings;
    private final FacetsConfig facetsConfig;

    public LuceneFacetedRecords(FacetedLuceneStorage facetedLuceneStorage, LuceneMappings luceneMappings, LuceneQueryPreprocessor luceneQueryPreprocessor) {
        this.facetedLuceneStorage = facetedLuceneStorage;
        this.facetsConfig = facetedLuceneStorage.facetsConfig();
        this.stringMappings = luceneMappings.stringMappings();
        this.lucene = new Lucene(luceneMappings.stringMappings());
        this.luceneQueryPreprocessor = luceneQueryPreprocessor;
    }

    @Override
    public <T extends Pair<Keyword<?>, Integer>> Sequence<Facet<FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests) throws IOException {
        return facetResults(predicate, facetsRequests, Sequences.<Group<Keyword<?>, String>>empty());
    }

    @Override
    public <T extends Pair<Keyword<?>, Integer>, S extends Group<Keyword<?>, String>> Sequence<Facet<FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests, Sequence<S> drillDowns) throws IOException {
        final IndexSearcher indexSearcher = luceneSearcher().searcher();
        final TaxonomyReader taxonomyReader = facetedLuceneStorage.taxonomyReader();
        final Query query = lucene.query(predicate);
        final Query processedQuery = new LuceneQueryVisitor(luceneQueryPreprocessor).visit(query);

        final DrillSideways drillSideways = new DrillSideways(indexSearcher, facetsConfig, taxonomyReader);
        final DrillDownQuery drillDownQuery = drillDownQuery(processedQuery, drillDowns);
        final DrillSidewaysResult drillSidewaysResult = drillSideways.search(drillDownQuery, indexSearcher.getIndexReader().maxDoc());

        return facetsRequests.map(new Mapper<Pair<Keyword<?>, Integer>, Facet<FacetEntry>>() {
            @Override
            public Facet<FacetEntry> call(Pair<Keyword<?>, Integer> facetRequest) throws Exception {
                final FacetResult facetResult = drillSidewaysResult.facets.getTopChildren(facetRequest.second(), facetRequest.first().name());
                final Sequence<FacetEntry> facetsAndCounts = (facetResult == null) ? Sequences.<FacetEntry>empty() : sequence(facetResult.labelValues).map(toLabelAndCountPair(facetRequest.first()));
                return Facet.facet(facetRequest.first(), facetsAndCounts);
            }
        });
    }

    private LuceneSearcher luceneSearcher() throws IOException {
        final Searcher searcher = facetedLuceneStorage.searcher();
        return (searcher instanceof ManagedSearcher) ? ((ManagedSearcher) searcher).luceneSearcher() : (LuceneSearcher) searcher;
    }

    private <S extends Group<Keyword<?>, String>> DrillDownQuery drillDownQuery(Query processedQuery, Sequence<S> drillDowns) {
        return drillDowns.fold(new DrillDownQuery(facetsConfig, processedQuery), new Callable2<DrillDownQuery, Group<Keyword<?>, String>, DrillDownQuery>() {
            @Override
            public DrillDownQuery call(DrillDownQuery drillDownQuery, Group<Keyword<?>, String> drillDown) throws Exception {
                for (String drillDownValue : drillDown) {
                    drillDownQuery.add(drillDown.key().name(), drillDownValue);
                }
                return drillDownQuery;
            }
        });
    }

    private Mapper<LabelAndValue, FacetEntry> toLabelAndCountPair(final Keyword<?> facetKeyword) {
        return new Mapper<LabelAndValue, FacetEntry>() {
            @Override
            public FacetEntry call(LabelAndValue labelAndValue) throws Exception {
                return FacetEntry.facetEntry(stringMappings.toValue(facetKeyword.forClass(), labelAndValue.label), labelAndValue.value);
            }
        };
    }
}
