package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.*;

import java.util.List;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Sequences.indexIn;
import static com.googlecode.totallylazy.Sequences.sequence;

public interface Definition extends Named, Comparable<Definition> {
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

    class functions {
        public static Callable1<Record, Record> sortFields(final Definition definition) {
            return new Callable1<Record, Record>() {
                @Override
                public Record call(Record record) throws Exception {
                    return Definition.methods.sortFields(definition, record);
                }
            };
        }
    }
}
