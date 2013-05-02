package com.googlecode.lazyrecords;

import com.googlecode.lazyparsec.Parser;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Strings;

import java.io.Reader;
import java.util.List;

import static com.googlecode.lazyparsec.Parsers.or;
import static com.googlecode.lazyparsec.Scanners.isChar;
import static com.googlecode.lazyparsec.Scanners.notChar;
import static com.googlecode.lazyrecords.parser.Grammar.ws;
import static com.googlecode.totallylazy.Sequences.sequence;

public interface CsvReader {

    Sequence<Record> read(Reader reader);

    enum constructors implements CsvReader {
        csvReader;

        private static final char QUOTE = '"';
        private static final char COMMA = ',';
        static final Function1<List<String>, String> join = new Function1<List<String>, String>() {
            public String call(List<String> strings) throws Exception {
                return sequence(strings).toString("");
            }
        };
        static final Parser<String> RAW = notChar(COMMA).many1().source();
        static final Parser<String> ESCAPED_QUOTE = isChar(QUOTE).times(2).retn("\"");
        static final Parser<String> QUOTED = or(notChar(QUOTE).source(), ESCAPED_QUOTE).many().map(join).between(isChar(QUOTE), isChar(QUOTE));
        static final Parser<String> TEXT = or(QUOTED, RAW);
        static final Parser<List<String>> FIELDS = TEXT.sepBy(ws(COMMA));

        @Override
        public Sequence<Record> read(Reader reader) {
            Sequence<String> allLines = Strings.lines(reader);
            Sequence<Keyword<String>> keywords = keywords(allLines.head());
            return records(keywords, allLines.tail());
        }

        private Sequence<Record> records(final Sequence<Keyword<String>> keywords, Sequence<String> rows) {
            return rows.map(new Mapper<String, Record>() {
                @Override
                public Record call(String row) throws Exception {
                    return Record.constructors.record(keywords.zip(FIELDS.parse(row)));
                }
            });
        }

        private Sequence<Keyword<String>> keywords(String header) {
            return sequence(FIELDS.parse(header)).map(keyword).realise();
        }

        private final Mapper<String, Keyword<String>> keyword = new Mapper<String, Keyword<String>>() {
            @Override
            public Keyword<String> call(String name) throws Exception {
                return Keyword.constructors.keyword(name, String.class);
            }
        };
    }
}
