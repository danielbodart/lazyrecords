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
        this.dateConverter = new DateFormatConverter(defaultConverter().formats().append(dateFormat()));
    }

    public static SimpleDateFormat dateFormat() {
        return Dates.format("yyyy/MM/dd");
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
