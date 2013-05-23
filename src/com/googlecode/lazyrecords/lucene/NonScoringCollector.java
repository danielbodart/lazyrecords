package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NonScoringCollector extends Collector {
    private final int maxDocs;
    private int base = 0;
    private final List<ScoreDoc> docs = new ArrayList<ScoreDoc>();

    public NonScoringCollector(int maxDocs) {
        this.maxDocs = maxDocs;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
    }

    @Override
    public void collect(int doc) throws IOException {
        if(docs.size() >= maxDocs) return;
        docs.add(new ScoreDoc(doc + base, 0));
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
        base = docBase;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    public TopDocs topDocs() {
        return new TopDocs(docs.size(), docs.toArray(new ScoreDoc[docs.size()]), 0);
    }
}
