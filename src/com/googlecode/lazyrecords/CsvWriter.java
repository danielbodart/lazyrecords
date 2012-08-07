package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import java.io.Writer;

public class CsvWriter {
    private static final String FIELD_SEPARATOR = ",";
    private static final char ROW_SEPARATOR = '\n';

    public static void writeTo(Sequence<Record> records, Writer writer, Sequence<Keyword<?>> requiredfields) {
        records.map(fieldsToString(requiredfields)).cons(requiredfields.map(Callables.asString()).toString()).fold(writer, writeLine());
    }

    private static Function2<Writer, String, Writer> writeLine() {
        return new Function2<Writer, String, Writer>() {
            @Override
            public Writer call(Writer writer, String line) throws Exception {
                return writer.append(line).append(ROW_SEPARATOR);
            }
        };
    }

    private static Function1<Record, String> fieldsToString(final Sequence<Keyword<?>> requiredFields) {
        return new Function1<Record, String>() {
            @Override
            public String call(Record record) throws Exception {
                Sequence<String> stringSequence = Sequences.sequence();
                for (Keyword<?> requiredField : requiredFields) {
                    Option<Object> option = Option.option(record.get(requiredField));
                    stringSequence = stringSequence.add(option.getOrElse("").toString());
                }
                return stringSequence.map(escapeSpecialCharacters()).toString(FIELD_SEPARATOR);
            }
        };
    }

    private static Function1<String, String> escapeSpecialCharacters() {
        return new Function1<String, String>() {
            @Override
            public String call(String recordValue) throws Exception {
                recordValue = recordValue.replace("\n", " ");
                if (recordValue.contains(",")) {
                    recordValue = "\"" + recordValue + "\"";
                }
                return recordValue;
            }
        };
    }
}