package com.googlecode.lazyrecords.lucene;

import java.io.Closeable;
import java.io.IOException;

public interface SearcherPool extends Closeable{
    Searcher searcher() throws IOException;

    void markAsDirty() throws IOException;
}
