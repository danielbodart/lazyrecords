package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Sequence;

import java.io.Writer;

import static com.googlecode.totallylazy.Callables.toString;

public class CsvWriter {
    private static final String FIELD_SEPARATOR = ", ";
    private static final char ROW_SEPARATOR = '\n';

    public static void writeTo(Definition definition, Sequence<Record> records, Writer writer) {
        records.map(fieldsToString(definition)).cons(headers(definition)).fold(writer, writeLine());
    }

    private static Function2<Writer, String, Writer> writeLine() {
        return new Function2<Writer, String, Writer>() {
            @Override
            public Writer call(Writer writer, String line) throws Exception {
                return writer.append(line).append(ROW_SEPARATOR);
            }
        };
    }

    private static Function1<Record, String> fieldsToString(final Definition definition) {
        return new Function1<Record, String>() {
            @Override
            public String call(Record record) throws Exception {
                return record.getValuesFor(definition.fields()).map(toString).map(escapeSpecialCharacters()).toString(FIELD_SEPARATOR);
            }
        };
    }

    private static String headers(Definition definition) {
        return definition.fields().toString(FIELD_SEPARATOR);
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
