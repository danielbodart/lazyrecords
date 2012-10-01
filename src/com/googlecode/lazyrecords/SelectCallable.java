package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import static com.googlecode.totallylazy.Sequences.sequence;

public class SelectCallable implements Callable1<Record, Record> {
    private final Sequence<Keyword<?>> keywords;

    protected SelectCallable(Sequence<Keyword<?>> keywords) {
        this.keywords = keywords;
    }

    public Record call(final Record source) throws Exception {
        return keywords.fold(Record.constructors.record(), new Callable2<Record, Keyword<?>, Record>() {
            @Override
            public Record call(Record record, Keyword<?> keyword) throws Exception {
                return record.set(Unchecked.<Keyword<Object>>cast(keyword), keyword.call(source));
            }
        });
    }

    public Sequence<Keyword<?>> keywords() {
        return keywords;
    }

    public static Callable1<? super Record, Record> select(final Keyword<?>... keywords) {
        return new SelectCallable(sequence(keywords));
    }

    public static Callable1<? super Record, Record> select(final Sequence<Keyword<?>> keywords) {
        return new SelectCallable(keywords);
    }

}
