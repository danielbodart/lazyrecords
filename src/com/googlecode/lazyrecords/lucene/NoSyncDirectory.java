package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;

public class NoSyncDirectory extends NIOFSDirectory {
    public NoSyncDirectory(File file) throws IOException {
        super(file);
    }

    @Override
    protected void fsync(String name) throws IOException {
        // This is called on every commit by Lucene. We don't care in tests, so overriding this method makes performance 25x faster
    }
}
