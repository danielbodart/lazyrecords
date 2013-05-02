package com.googlecode.lazyrecords;

import com.googlecode.lazyparsec.Parser;
import com.googlecode.lazyparsec.Parsers;
import com.googlecode.lazyparsec.Scanners;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Sequence;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static com.googlecode.lazyparsec.Parsers.or;
import static com.googlecode.lazyparsec.Scanners.among;
import static com.googlecode.lazyparsec.Scanners.isChar;
import static com.googlecode.lazyparsec.Scanners.notAmong;
import static com.googlecode.lazyparsec.Scanners.notChar;
import static com.googlecode.lazyrecords.parser.Grammar.ws;
import static com.googlecode.totallylazy.LazyException.lazyException;
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
        static final Parser<String> RAW = notAmong(",\n").many1().source();
        static final Parser<String> ESCAPED_QUOTE = isChar(QUOTE).times(2).retn("\"");
        static final Parser<String> QUOTED = or(notChar(QUOTE).source(), ESCAPED_QUOTE).many().map(join).between(isChar(QUOTE), isChar(QUOTE));
        static final Parser<String> TEXT = or(QUOTED, RAW);
        static final Parser<List<String>> FIELDS = TEXT.sepBy(ws(COMMA));
        static final Parser<List<List<String>>> ROW = FIELDS.sepBy(among("\n\r").many());

        @Override
        public Sequence<Record> read(Reader reader) {
            try {
                List<List<String>> allLines = ROW.parse(reader);
                Sequence<Keyword<String>> keywords = keywords(allLines.get(0));
                return records(keywords, allLines.subList(1, allLines.size()));
            } catch (IOException e) {
                throw lazyException(e);
            }
        }

        private Sequence<Record> records(final Sequence<Keyword<String>> keywords, Iterable<List<String>> rows) {
            return sequence(rows).map(new Mapper<List<String>, Record>() {
                @Override
                public Record call(List<String> fields) throws Exception {
                    return Record.constructors.record(keywords.zip(fields));
                }
            }).realise();
        }

        private Sequence<Keyword<String>> keywords(Iterable<String> parse) {
            return sequence(parse).map(keyword).realise();
        }

        private final Mapper<String, Keyword<String>> keyword = new Mapper<String, Keyword<String>>() {
            @Override
            public Keyword<String> call(String name) throws Exception {
                return Keyword.constructors.keyword(name, String.class);
            }
        };
    }
}
