package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import org.junit.Test;

import java.util.Date;

import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.MapRecord.record;
import static com.googlecode.lazyrecords.RecordMethods.filter;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecordMethodsTest {
    protected static Keyword<Integer> age = keyword("age", Integer.class);
    protected static Keyword<Date> dob = keyword("dob", Date.class);
    protected static ImmutableKeyword<String> firstName = keyword("firstName", String.class);
    protected static Keyword<String> lastName = keyword("lastName", String.class);

    @Test
    public void canEasilyFilterFields() throws Exception {
        Record original = record().set(age, 12).set(firstName, "dan");
        Record newRecord = filter(original, age);
        assertThat(newRecord.fields(), hasExactly(Pair.<Keyword<?>, Object>pair(age, 12)));
    }
}
