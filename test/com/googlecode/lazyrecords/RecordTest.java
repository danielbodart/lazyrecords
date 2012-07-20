package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import org.junit.Test;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.methods.filter;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecordTest {
    protected static Keyword<Integer> age = keyword("age", Integer.class);
    protected static ImmutableKeyword<String> firstName = keyword("firstName", String.class);

    @Test
    public void canEasilyFilterFields() throws Exception {
        Record original = Record.constructors.record().set(age, 12).set(firstName, "dan");
        Record newRecord = filter(original, age);
        assertThat(newRecord.fields(), hasExactly(Pair.<Keyword<?>, Object>pair(age, 12)));
    }
}
