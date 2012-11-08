package com.googlecode.lazyrecords.lucene;

import java.io.File;
import java.io.IOException;

public interface Persistence {
    void deleteAll() throws IOException;

    void backup(File destination) throws Exception;

    void restore(File file) throws Exception;
}
