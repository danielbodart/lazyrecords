package com.googlecode.lazyrecords;

import org.junit.Test;

import static com.googlecode.lazyrecords.RecordsContract.People;
import static com.googlecode.lazyrecords.RecordsContract.People.*;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefinitionTest {

    @Test
    public void canCreateDefinitionsFromInterface() throws Exception {
        assertThat(people.name(), is("people"));
        assertThat(people.fields(), hasExactly((Keyword<?>) age, dob, firstName, lastName, isbn));
    }

    @Test
    public void canSupplyCustomName() throws Exception {
        assertThat(constructors.definition(People.class, "Foo").name(), is("Foo"));
    }
}
