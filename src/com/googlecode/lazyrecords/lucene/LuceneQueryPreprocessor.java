package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;

public interface LuceneQueryPreprocessor {
    Query process(TermQuery query);

    Query process(WildcardQuery query);

    Query process(PhraseQuery query);

    Query process(PrefixQuery query);

    Query process(MultiPhraseQuery query);

    Query process(FuzzyQuery query);

    Query process(RegexpQuery query);

    Query process(TermRangeQuery query);

    Query process(NumericRangeQuery query);

    Query process(MatchAllDocsQuery query);
}
