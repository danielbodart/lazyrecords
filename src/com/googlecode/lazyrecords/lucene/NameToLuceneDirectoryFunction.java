package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Value;
import org.apache.lucene.store.Directory;

public class NameToLuceneDirectoryFunction implements Value<Function1<String, Directory>> {
    private Function1<String, Directory> activator;

    public NameToLuceneDirectoryFunction(Function1<String, Directory> activator) {
        this.activator = activator;
    }

    @Override
    public Function1<String, Directory> value() {
        return activator;
    }
}
