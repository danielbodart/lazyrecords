package com.googlecode.lazyrecords;

import org.junit.Test;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class RecordContract {
    protected abstract Record createRecord();

    private Keyword<String> firstName = Keywords.keyword("firstName", String.class);

    @Test
    public void supportsEquality() throws Exception {
        Record dan = createRecord().set(firstName, "dan");
        Record dansDouble = createRecord().set(firstName, "dan");
        Record mat = createRecord().set(firstName, "mat");
        Record nullName = createRecord().set(firstName, null);

        assertThat(dan.equals(dansDouble), is(true));
        assertThat(nullName.equals(nullName), is(true));
        assertThat(dan.equals(mat), is(false));
        assertThat(dan.equals(nullName), is(false));
    }


}
