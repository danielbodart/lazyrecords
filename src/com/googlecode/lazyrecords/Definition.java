package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;

import java.util.List;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.indexIn;
import static com.googlecode.totallylazy.Sequences.sequence;

public interface Definition extends Named {
    Sequence<Keyword<?>> fields();

    class constructors {
        public static Definition definition(final String name, final Iterable<? extends Keyword<?>> fields) {
            return new RecordDefinition(name, sequence(fields));
        }

        public static Definition definition(final String name, final Keyword<?> head, final Keyword<?>... tail) {
            return new RecordDefinition(name, sequence(tail).cons(head));
        }
    }

    class methods {
        public static Record sortFields(Definition definition, Record record) {
            return record(record.fields().sortBy(sameOrderAs(definition)));
        }

        private static Function1<First<Keyword<?>>, Integer> sameOrderAs(final Definition definition) {
            return Callables.<Keyword<?>>first().then(indexIn(definition.fields()));
        }
    }
}
