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

public interface QueryVisitor {
    Query visit(TermQuery query);

    Query visit(WildcardQuery query);

    Query visit(PhraseQuery query);

    Query visit(PrefixQuery query);

    Query visit(MultiPhraseQuery query);

    Query visit(FuzzyQuery query);

    Query visit(RegexpQuery query);

    Query visit(TermRangeQuery query);

    Query visit(NumericRangeQuery query);

    Query visit(MatchAllDocsQuery query);
}
