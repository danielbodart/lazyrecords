package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.search.Query;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public interface LuceneStorage extends Closeable, Flushable, Persistence {
    Number add(Sequence<Document> documents) throws IOException;

    Number delete(Query query) throws IOException;

    void deleteNoCount(Query query) throws IOException;

    int count(Query query) throws IOException;

    <T> T search(Callable1<Searcher, T> callable) throws IOException;

    Searcher searcher() throws IOException;

    CheckIndex.Status check() throws IOException;

    void fix() throws IOException;

}
