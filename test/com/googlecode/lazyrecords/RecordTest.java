package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Pair;
import org.junit.Test;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.Record.constructors.record;
import static com.googlecode.lazyrecords.Record.methods.filter;
import static com.googlecode.lazyrecords.Record.methods.merge;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RecordTest {
    protected static Keyword<Integer> age = keyword("age", Integer.class);
    protected static ImmutableKeyword<String> firstName = keyword("firstName", String.class);
    protected static ImmutableKeyword<String> lastName = keyword("lastName", String.class);

    @Test
    public void canEasilyFilterFields() throws Exception {
        Record original = record().set(age, 12).set(firstName, "dan");
        Record newRecord = filter(original, age);
        assertThat(newRecord.fields(), hasExactly(Pair.<Keyword<?>, Object>pair(age, 12)));
    }

	@Test
	public void supportsMergingRecords() {
		Record first = record().set(age, 10);
		Record second = record().set(firstName, "ray");
		Record third = record().set(lastName, "barlow");

		Record result = merge(first, second, third);

		assertThat(result.get(age), is(10));
		assertThat(result.get(firstName), is("ray"));
		assertThat(result.get(lastName), is("barlow"));
	}

}
