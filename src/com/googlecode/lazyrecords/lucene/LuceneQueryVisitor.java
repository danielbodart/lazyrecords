package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.multi;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
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

public class LuceneQueryVisitor {

    private final LuceneQueryPreprocessor preprocessor;

    public LuceneQueryVisitor(LuceneQueryPreprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }

    public Query visit(Query query) {
        return new multi() {}.<Query>methodOption(query).getOrThrow(new UnsupportedOperationException());
    }

    @multimethod private Query visit(BooleanQuery query) {
        BooleanQuery visitedQuery = new BooleanQuery();
        for (BooleanClause clause : query) {
            visitedQuery.add(visit(clause.getQuery()), clause.getOccur());
        }
        return visitedQuery;
    }

    @multimethod private Query visit(ConstantScoreQuery query) {
        if (query.getQuery() == null) {
            throw new UnsupportedOperationException();
        }
        return new ConstantScoreQuery(visit(query.getQuery()));
    }

    @multimethod private Query visit(DisjunctionMaxQuery query) {
        final DisjunctionMaxQuery visitedQuery = new DisjunctionMaxQuery(query.getTieBreakerMultiplier());
        for (Query disjunctionQuery : query) {
            visitedQuery.add(visit(disjunctionQuery));
        }
        return visitedQuery;
    }

    @multimethod private Query visit(TermQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(WildcardQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(PhraseQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(PrefixQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(MultiPhraseQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(FuzzyQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(RegexpQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(TermRangeQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(NumericRangeQuery query) { return preprocessor.process(query); }

    @multimethod private Query visit(MatchAllDocsQuery query) { return preprocessor.process(query); }

}
