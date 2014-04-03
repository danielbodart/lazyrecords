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

public class DoNothingQueryVisitor implements QueryVisitor {

    @Override
    public Query visit(TermQuery query) {
        return query;
    }

    @Override
    public Query visit(WildcardQuery query) {
        return query;
    }

    @Override
    public Query visit(PhraseQuery query) {
        return query;
    }

    @Override
    public Query visit(PrefixQuery query) {
        return query;
    }

    @Override
    public Query visit(MultiPhraseQuery query) {
        return query;
    }

    @Override
    public Query visit(FuzzyQuery query) {
        return query;
    }

    @Override
    public Query visit(RegexpQuery query) {
        return query;
    }

    @Override
    public Query visit(TermRangeQuery query) {
        return query;
    }

    @Override
    public Query visit(NumericRangeQuery query) {
        return query;
    }

    @Override
    public Query visit(MatchAllDocsQuery query) {
        return query;
    }
}
