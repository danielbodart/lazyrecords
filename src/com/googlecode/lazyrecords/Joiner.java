package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.BinaryPredicate;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Predicate;

public interface Joiner extends BinaryPredicate<Record>, Callable1<Record, Predicate<Record>> {
}
