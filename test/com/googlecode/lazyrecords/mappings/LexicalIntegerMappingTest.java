package com.googlecode.lazyrecords.mappings;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LexicalIntegerMappingTest {
    @Test
    public void supportsToString() throws Exception {
        assertThat(new LexicalIntegerMapping().toString(Integer.MIN_VALUE), is("0000000000"));
        assertThat(new LexicalIntegerMapping().toString(Integer.MAX_VALUE), is("4294967295"));
    }

    @Test
    public void supportsToValue() throws Exception {
        assertThat(new LexicalIntegerMapping().toValue("0000000000"), is(Integer.MIN_VALUE));
        assertThat(new LexicalIntegerMapping().toValue("4294967295"), is(Integer.MAX_VALUE));
    }
}
