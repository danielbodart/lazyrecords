package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.search.Query;

import java.io.IOException;

import static com.googlecode.lazyrecords.Facet.FacetEntry;

public interface FacetedRecords {
    <T extends Pair<Keyword<?>, Integer>> Sequence<Facet<FacetEntry>> facetResults(Predicate<? super Record> predicate, Sequence<T> facetsRequests) throws IOException;
}
