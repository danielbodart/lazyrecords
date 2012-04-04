package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;

import java.util.List;

import static com.googlecode.lazyrecords.Record.constructors.record;
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
            final List<Keyword<?>> keywords = definition.fields().toList();
            Sequence<Pair<Keyword<?>, Object>> sortedFields = record.fields().sortBy(sameOrderAs(keywords));
            return record(sortedFields);
        }

        private static Callable1<Pair<Keyword<?>, Object>, Integer> sameOrderAs(final List<Keyword<?>> keywords) {
            return new Callable1<Pair<Keyword<?>, Object>, Integer>() {
                @Override
                public Integer call(Pair<Keyword<?>, Object> keywordObjectPair) throws Exception {
                    return keywords.indexOf(keywordObjectPair.first());
                }
            };
        }
    }
}
