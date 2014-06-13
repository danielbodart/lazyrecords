package com.googlecode.lazyrecords.parser;

import com.googlecode.totallylazy.time.DateConverter;
import com.googlecode.totallylazy.time.DateFormatConverter;
import com.googlecode.totallylazy.time.Dates;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.googlecode.totallylazy.time.DateFormatConverter.defaultConverter;

public class ParserDateConverter implements DateConverter {
    private final DateConverter dateConverter;

    public ParserDateConverter(DateConverter dateConverter) {
        this.dateConverter = dateConverter;
    }

    public ParserDateConverter() {
        this(new DateFormatConverter(defaultConverter().formats()
                .append(ukShortDateTimeFormat())
                .append(ukShortDateFormat())
                .append(dateTimeFormat())
                .append(dateFormat())));
    }

    public static SimpleDateFormat ukShortDateTimeFormat() {
        return Dates.format("dd/MM/yy HH:mm:ss");
    }

    public static SimpleDateFormat ukShortDateFormat() {
        return Dates.format("dd/MM/yy");
    }

    public static SimpleDateFormat dateTimeFormat() {
        return Dates.format("yy/MM/dd HH:mm:ss");
    }

    public static SimpleDateFormat dateFormat() {
        return Dates.format("yy/MM/dd");
    }

    @Override
    public String format(Date value) {
        return dateConverter.format(value);
    }

    @Override
    public Date parse(String value) {
        return dateConverter.parse(value);
    }
}