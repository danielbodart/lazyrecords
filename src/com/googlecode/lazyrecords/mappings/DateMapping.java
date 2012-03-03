package com.googlecode.lazyrecords.mappings;

import com.googlecode.totallylazy.time.DateConverter;
import com.googlecode.totallylazy.time.DateFormatConverter;
import com.googlecode.totallylazy.time.Dates;

import java.text.ParseException;
import java.util.Date;

public class DateMapping implements StringMapping<Date> {
    private final DateConverter converter;

    public DateMapping(DateConverter converter) {
        this.converter = converter;
    }

    public DateMapping() {
        this(DateFormatConverter.defaultConverter());
    }

    public Date toValue(String value) throws ParseException {
        return converter.parse(value);
    }

    public String toString(Date value) {
        return converter.format(value);
    }

    public static DateMapping atomDateFormat() {
        return new DateMapping(Dates.RFC3339());
    }

    public static DateMapping rssDateFormat() {
        return new DateMapping(new DateFormatConverter(Dates.RFC822()));
    }

}
