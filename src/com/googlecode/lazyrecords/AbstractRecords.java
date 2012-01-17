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
    private final Map<RecordName, List<Keyword<?>>> definitions = new HashMap<RecordName, List<Keyword<?>>>();

    public void define(RecordName recordName, Keyword<?>... fields) {
        definitions.put(recordName, list(fields));
    }

    public List<Keyword<?>> undefine(RecordName recordName){
        return definitions.remove(recordName);
    }

    public Sequence<Keyword<?>> definitions(RecordName recordName) {
        if (!definitions.containsKey(recordName)) {
            return Sequences.empty();
        }
        return sequence(definitions.get(recordName));
    }

    // Only override if you Schema based technology like SQL
    public boolean exists(RecordName recordName) {
        return true;
    }

    public Number add(RecordName recordName, Record... records) {
        if (records.length == 0) return 0;
        return add(recordName, sequence(records));
    }

    public Number set(final RecordName recordName, Pair<? extends Predicate<? super Record>, Record>... records) {
        return set(recordName, sequence(records));
    }

    public Number set(final RecordName recordName, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return records.map(update(recordName, false)).reduce(sum());
    }

    public Number put(final RecordName recordName, Pair<? extends Predicate<? super Record>, Record>... records) {
        return put(recordName, sequence(records));
    }

    public Number put(final RecordName recordName, Sequence<? extends Pair<? extends Predicate<? super Record>, Record>> records) {
        return records.map(update(recordName, true)).reduce(sum());
    }

    private Function1<Pair<? extends Predicate<? super Record>, Record>, Number> update(final RecordName recordName, final boolean add) {
        return new Function1<Pair<? extends Predicate<? super Record>, Record>, Number>() {
            public Number call(Pair<? extends Predicate<? super Record>, Record> pair) throws Exception {
                Predicate<? super Record> predicate = pair.first();
                Sequence<Record> matched = get(recordName).filter(predicate).realise();
                if (add && matched.isEmpty()) {
                    return add(recordName, pair.second());
                }
                remove(recordName, predicate);
                return add(recordName, matched.map(merge(pair.second())));
            }
        };
    }

    public Number remove(RecordName recordName) {
        return remove(recordName, all(Record.class));
    }
}
