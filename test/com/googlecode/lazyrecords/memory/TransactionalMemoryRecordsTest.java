package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.RecordsContract;
import org.junit.Test;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TransactionalMemoryRecordsTest extends RecordsContract<TransactionalMemoryRecords> {
    private TransactionalMemory datastore;

    public TransactionalMemoryRecords createRecords() {
        datastore = new TransactionalMemory();
        return new TransactionalMemoryRecords(datastore);
    }

    @Test
    public void canCommit() throws Exception {
        assertThat(records.get(people).isEmpty(), is(false));
        assertThat(datastore.value().get(people).isEmpty(), is(true));
        records.commit();
        assertThat(datastore.value().get(people).isEmpty(), is(false));
    }

    @Test
    public void canRollback() throws Exception {
        records.commit();
        assertThat(records.get(people).isEmpty(), is(false));
        records.remove(people);
        assertThat(records.get(people).isEmpty(), is(true));
        records.rollback();
        assertThat(records.get(people).isEmpty(), is(false));
    }

}
