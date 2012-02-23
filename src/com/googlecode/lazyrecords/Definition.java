package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;

public interface Definition {
    String name();
    Sequence<Keyword<?>> fields();
}
