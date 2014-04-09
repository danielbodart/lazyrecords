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

public class DoNothingLuceneQueryPreprocessor implements LuceneQueryPreprocessor {

    @Override
    public Query process(TermQuery query) {
        return query;
    }

    @Override
    public Query process(WildcardQuery query) {
        return query;
    }

    @Override
    public Query process(PhraseQuery query) {
        return query;
    }

    @Override
    public Query process(PrefixQuery query) {
        return query;
    }

    @Override
    public Query process(MultiPhraseQuery query) {
        return query;
    }

    @Override
    public Query process(FuzzyQuery query) {
        return query;
    }

    @Override
    public Query process(RegexpQuery query) {
        return query;
    }

    @Override
    public Query process(TermRangeQuery query) {
        return query;
    }

    @Override
    public Query process(NumericRangeQuery query) {
        return query;
    }

    @Override
    public Query process(MatchAllDocsQuery query) {
        return query;
    }
}
