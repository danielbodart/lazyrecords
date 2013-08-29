package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Fields;
import com.googlecode.totallylazy.First;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.totallylazy.Predicates.classAssignableTo;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.indexIn;
import static com.googlecode.totallylazy.Sequences.sequence;

public interface Definition extends Named, Metadata<Definition>, Comparable<Definition> {
    Sequence<Keyword<?>> fields();

    Definition as(String name);

    class constructors {
        public static <T extends Definition> T definition(final Class<T> definition) {
            return definition(definition, definition.getSimpleName().toLowerCase());
        }

        public static <T extends Definition> T definition(Class<T> definition, String name) {
            final RecordDefinition recordDefinition = new RecordDefinition(name, record(), fields(definition));
            return Unchecked.<T>cast(Proxy.newProxyInstance(Definition.class.getClassLoader(), new Class[]{definition}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(recordDefinition, args);
                }
            }));
        }

        private static <T extends Definition> Sequence<Keyword<?>> fields(Class<T> definition) {
            return sequence(definition.getDeclaredFields()).
                    filter(where(Fields.type, is(classAssignableTo(Keyword.class)))).
                    map(Fields.value(null)).
                    unsafeCast();
        }

        public static Definition definition(final String name, Record metadata, final Iterable<? extends Keyword<?>> fields) {
            return new RecordDefinition(name, metadata, sequence(fields));
        }

        public static Definition definition(final String name, final Iterable<? extends Keyword<?>> fields) {
            return definition(name, record(), sequence(fields));
        }

        public static Definition definition(final String name, final Keyword<?> head, final Keyword<?>... tail) {
            return definition(name, sequence(tail).cons(head));
        }
    }

    class methods {
        public static Record sortFields(Definition definition, Record record) {
            return record(record.fields().sortBy(sameOrderAs(definition)));
        }

        private static Function1<First<Keyword<?>>, Integer> sameOrderAs(final Definition definition) {
            return Callables.<Keyword<?>>first().then(indexIn(definition.fields()));
        }

        public static Definition replace(Definition definition, Keyword<?> from, Keyword<?> to) {
            return definition(definition.name(), definition.fields().map(Keyword.functions.replace(from, to)));
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

        public static Mapper<Definition, Sequence<Keyword<?>>> fields = new Mapper<Definition, Sequence<Keyword<?>>>() {
            @Override
            public Sequence<Keyword<?>> call(Definition definition) throws Exception {
                return definition.fields();
            }
        };

        public static Mapper<Definition, Sequence<Keyword<?>>> fields() {
            return fields;
        }

        public static Mapper<Named, String> name = new Mapper<Named, String>() {
            @Override
            public String call(Named definition) throws Exception {
                return definition.name();
            }
        };

        public static Mapper<Named, String> name() {
            return name;
        }
    }
}
