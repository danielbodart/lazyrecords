package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.totallylazy.Arrays.list;
import static com.googlecode.totallylazy.Predicates.all;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.sum;
import static com.googlecode.lazyrecords.RecordMethods.merge;

public abstract class AbstractRecords implements Records {
    private final Map<Definition, List<Keyword<?>>> definitions = new HashMap<Definition, List<Keyword<?>>>();

    public void define(Definition definition, Keyword<?>... fields) {
        definitions.put(definition, list(fields));
    }

    public List<Keyword<?>> undefine(Definition definition){
        return definitions.remove(definition);
    }

    public Sequence<Keyword<?>> definitions(Definition definition) {
        if (!definitions.containsKey(definition)) {
            return Sequences.empty();
        }
        return sequence(definitions.get(definition));
    }

    // Only override if you Schema based technology like SQL
    public boolean exists(Definition definition) {
        return true;
    }

    public Number add(Definition definition, Record... records) {
        if (records.length == 0) return 0;
        return add(definition, sequence(records));
    }

    public Number set(final Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return set(definition, sequence(records));
    }

    public Number set(final Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return records.map(update(definition, false)).reduce(sum());
    }

    public Number put(final Definition definition, Pair<? extends Predicate<? super Record>, Record>... records) {
        return put(definition, sequence(records));
    }

    public Number put(final Definition definition, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return records.map(update(definition, true)).reduce(sum());
    }

    private Function1<Pair<? extends Predicate<? super Record>, Record>, Number> update(final Definition definition, final boolean add) {
        return new Function1<Pair<? extends Predicate<? super Record>, Record>, Number>() {
            public Number call(Pair<? extends Predicate<? super Record>, Record> pair) throws Exception {
                Predicate<? super Record> predicate = pair.first();
                Sequence<Record> matched = get(definition).filter(predicate).realise();
                if (add && matched.isEmpty()) {
                    return add(definition, pair.second());
                }
                remove(definition, predicate);
                return add(definition, matched.map(merge(pair.second())));
            }
        };
    }

    public Number remove(Definition definition) {
        return remove(definition, all(Record.class));
    }
}
