package com.googlecode.lazyrecords.mappings;

import org.junit.Test;

import java.util.Formatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EnumMappingTest {
    @Test
    public void canConvertAnyEnum() throws Exception {
        LexicalMapping<Formatter.BigDecimalLayoutForm> mapping = new EnumMapping<Formatter.BigDecimalLayoutForm>(Formatter.BigDecimalLayoutForm.class);
        Formatter.BigDecimalLayoutForm enumValue = Formatter.BigDecimalLayoutForm.DECIMAL_FLOAT;

        String stringValue = mapping.toString(enumValue);
        assertThat(stringValue, is(enumValue.name()));

        Formatter.BigDecimalLayoutForm result = mapping.toValue(stringValue);
        assertThat(result, is(enumValue));
    }
}
