package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import java.io.Closeable;
import java.io.IOException;

public interface Searcher extends Closeable{

    TopDocs search(Query query, Sort sort) throws IOException;

    TopDocs search(Query query, Sort sort, int end) throws IOException;

    void search(Query query, Collector collector) throws IOException;

    Document document(int id) throws IOException;

    int count(Query query) throws IOException;
}
