package com.googlecode.lazyrecords.lucene;

import java.io.Closeable;

public interface NameToLuceneStorageFunction extends Closeable {
    LuceneStorage getForName(String name);
}
