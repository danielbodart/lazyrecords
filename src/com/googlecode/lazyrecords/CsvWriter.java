package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Iterators;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import java.io.Writer;
import java.util.Iterator;

import static com.googlecode.totallylazy.Callables.toString;

public class CsvWriter {
    private static final String FIELD_SEPARATOR = ",";
    private static final char ROW_SEPARATOR = '\n';

    public static void writeTo(Iterator<Record> records, Writer writer, Sequence<Keyword<?>> fields) {
        Iterators.fold(Iterators.cons(headers(fields), Iterators.map(records, rowToString(fields))), writer, writeLine());
    }

    private static Function2<Writer, String, Writer> writeLine() {
        return new Function2<Writer, String, Writer>() {
            @Override
            public Writer call(Writer writer, String line) throws Exception {
                return writer.append(line).append(ROW_SEPARATOR);
            }
        };
    }

    private static Function1<Record, String> rowToString(final Sequence<Keyword<?>> fields) {
        return new Function1<Record, String>() {
            @Override
            public String call(final Record record) throws Exception {
                return fields.map(fieldToString(record)).map(escapeSpecialCharacters()).toString(FIELD_SEPARATOR);
            }
        };
    }

    private static Callable1<Keyword<?>, String> fieldToString(final Record record) {
        return new Callable1<Keyword<?>, String>() {
            @Override
            public String call(Keyword<?> keyword) throws Exception {
                return Option.option(record.get(keyword)).map(toString).getOrElse("");
            }
        };
    }

    private static Function1<String, String> escapeSpecialCharacters() {
        return new Function1<String, String>() {
            @Override
            public String call(String recordValue) throws Exception {
                recordValue = recordValue.replace('\n', ' ');
                if (recordValue.contains(",")) {
                    return '"' + recordValue + '"';
                }
                return recordValue;
            }
        };
    }

    private static String headers(Sequence<Keyword<?>> fields) {
        return fields.map(toString).toString(FIELD_SEPARATOR);
    }
}