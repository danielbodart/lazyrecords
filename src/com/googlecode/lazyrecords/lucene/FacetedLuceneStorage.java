package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;

import java.io.IOException;

public interface FacetedLuceneStorage extends LuceneStorage {
    TaxonomyReader taxonomyReader() throws IOException;
    FacetsConfig facetsConfig();
}
