package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Value;

import static com.googlecode.totallylazy.Sequences.sequence;

public interface Definition extends Named{
    Sequence<Keyword<?>> fields();

    public static final class constructors{
        public static Definition definition(final String name, final Iterable<? extends Keyword<?>> fields) {
            return new RecordDefinition(name, sequence(fields));
        }

        public static Definition definition(final String name, final Keyword<?> head, final Keyword<?>... tail) {
            return new RecordDefinition(name, sequence(tail).cons(head));
        }
    }
}
