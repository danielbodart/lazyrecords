package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import com.googlecode.lazyrecords.AbstractRecordsTests;
import com.googlecode.lazyrecords.Keyword;
import org.junit.Test;

import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.MapRecord.record;
import static org.hamcrest.MatcherAssert.assertThat;

public class MemoryRecordsTest extends AbstractRecordsTests<MemoryRecords> {
    private static final RecordName TREES = RecordName.recordName("some_table");
    private static final Keyword<String> LEAFINESS = keyword("some_field", String.class);

    public MemoryRecords createRecords() {
        return new MemoryRecords();
    }

    @Test
    public void willNotFailIfAskedToRemoveATableWhichHasNotBeenAddedTo() throws Exception {
        MemoryRecords records = new MemoryRecords();
        RecordName table = TREES;
        records.define(table, LEAFINESS);
        records.remove(table);
    }

    @Test
    public void allowsAddingWithoutDefiningAKeyword() throws Exception {
        MemoryRecords records = new MemoryRecords();
        records.add(TREES, record().set(LEAFINESS, "a very leafy tree"));
        assertThat(records.get(TREES).filter(where(LEAFINESS, is("a very leafy tree"))).size(), NumberMatcher.is(1));
    }

    @Test
    public void allowsRemovingWithoutDefiningAKeyword() throws Exception {
        MemoryRecords records = new MemoryRecords();
        assertThat(records.remove(TREES), NumberMatcher.is(0));
    }

    @Test
    public void allowsGettingWithoutDefiningAKeyword() throws Exception {
        MemoryRecords records = new MemoryRecords();
        assertThat(
                records.get(TREES).filter(where(LEAFINESS, is("don't be silly there are no records"))).size(),
                NumberMatcher.is(0));
    }
}
