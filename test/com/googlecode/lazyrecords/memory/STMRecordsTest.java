package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.RecordsContract;
import org.junit.Test;

import static com.googlecode.lazyrecords.RecordsContract.People.people;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class STMRecordsTest extends RecordsContract<STMRecords> {
    private STM stm;

    public STMRecords createRecords() {
        stm = new STM();
        return new STMRecords(stm);
    }

    @Test
    public void canCommit() throws Exception {
        assertThat(records.get(people).isEmpty(), is(false));
        assertThat(stm.value().lookup(people).isEmpty(), is(true));
        records.commit();
        assertThat(stm.value().lookup(people).isEmpty(), is(false));
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
