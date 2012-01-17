package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyparsec.Parser;
import com.googlecode.lazyparsec.Parsers;
import com.googlecode.lazyparsec.pattern.CharacterPredicates;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Triple;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.Seconds;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static com.googlecode.lazyparsec.Scanners.isChar;
import static com.googlecode.lazyparsec.Scanners.notChar;
import static com.googlecode.lazyparsec.Scanners.pattern;
import static com.googlecode.lazyparsec.pattern.Patterns.regex;
import static com.googlecode.totallylazy.Predicates.or;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.contains;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.startsWith;
import static com.googlecode.lazyrecords.Keywords.keyword;

@SuppressWarnings("unchecked")
public class Grammar {

    private static Parser<Void> ws(char value) {
        return ws(String.valueOf(value));
    }

    private static Parser<Void> ws(String value) {
        return pattern(regex(String.format("\\s*%s\\s*", value)), value);
    }

    public static final DateFormat DATE_FORMAT = Dates.format("yyyy/MM/dd");
    public static final Parser<Date> DATE = pattern(regex("\\d{4}/\\d{1,2}/\\d{1,2}"), "date").source().map(new Callable1<String, Date>() {
        public Date call(String value) throws Exception {
            return DATE_FORMAT.parse(value);
        }
    });
    public static final Parser<String> TEXT = isChar(CharacterPredicates.IS_ALPHA_NUMERIC).many1().source();
    public static final Parser<String> QUOTED_TEXT = notChar('"').many1().source().between(isChar('"'), isChar('"'));
    public static final Parser<String> TEXT_ONLY = Parsers.or(QUOTED_TEXT, TEXT);
    public static final Parser<Comparable> VALUES = Parsers.<Comparable>or(DATE, TEXT_ONLY);
    public static final Parser<String> NAME = TEXT_ONLY;
    public static final Parser<Void> WILDCARD = isChar('*');
    public static final Parser<Void> GT = ws('>');
    public static final Parser<Void> GTE = ws(">=");
    public static final Parser<Void> LT = ws('<');
    public static final Parser<Void> LTE = ws("<=");
    public static final Parser<Void> OPERATORS = Parsers.or(GTE, GT, LTE, LT);

    public static final Parser<Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>> OR = ws("OR").map(new Callable1<Void, Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>>() {
        public Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>> call(Void aVoid) throws Exception {
            return new Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>() {
                public Predicate<Record> call(Predicate<Record> p1, Predicate<Record> p2) throws Exception {
                    return Predicates.or(p1, p2);
                }
            };
        }
    });
    public static final Parser<Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>> AND = ws("AND").or(isChar(' ').skipMany()).map(new Callable1<Void, Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>>() {
        public Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>> call(Void aVoid) throws Exception {
            return new Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>() {
                public Predicate<Record> call(Predicate<Record> p1, Predicate<Record> p2) throws Exception {
                    return Predicates.and(p1, p2);
                }
            };
        }
    });

    public static final Parser<Pair<Class, Predicate>> TEXT_STARTS_WITH = TEXT.followedBy(WILDCARD).map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, startsWith(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> TEXT_ENDS_WITH = Parsers.sequence(WILDCARD, TEXT).map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, endsWith(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> TEXT_CONTAINS = TEXT.between(WILDCARD, WILDCARD).map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, contains(value));
        }
    });

    private static Parser<Callable1<Predicate<Record>, Predicate<Record>>> NEGATION = ws('-').or(ws("NOT")).optional().map(new Callable1<Void, Callable1<Predicate<Record>, Predicate<Record>>>() {
        public Callable1<Predicate<Record>, Predicate<Record>> call(Void aVoid) throws Exception {
            return new Callable1<Predicate<Record>, Predicate<Record>>() {
                public Predicate<Record> call(Predicate<Record> predicate) throws Exception {
                    return Predicates.not(predicate);
                }
            };
        }
    });

    public static final Parser<Pair<Class, Predicate>> DATE_IS = DATE.map(new Callable1<Date, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Date dateWithoutTime) throws Exception {
            Date upper = Seconds.add(dateWithoutTime, (24 * 60 * 60) - 1);
            return Pair.<Class, Predicate>pair(Date.class, Predicates.between(dateWithoutTime, upper));
        }
    });

    public static final Parser<Pair<Class, Predicate>> GREATER_THAN = Parsers.sequence(GT, VALUES).map(new Callable1<Comparable, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Comparable value) throws Exception {
            return Pair.<Class, Predicate>pair(value.getClass(), Predicates.greaterThan(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> GREATER_THAN_OR_EQUALS = Parsers.sequence(GTE, VALUES).map(new Callable1<Comparable, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Comparable value) throws Exception {
            return Pair.<Class, Predicate>pair(value.getClass(), Predicates.greaterThanOrEqualTo(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> LESS_THAN_OR_EQUALS = Parsers.sequence(LTE, VALUES).map(new Callable1<Comparable, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Comparable value) throws Exception {
            return Pair.<Class, Predicate>pair(value.getClass(), Predicates.lessThanOrEqualTo(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> LESS_THAN = Parsers.sequence(LT, VALUES).map(new Callable1<Comparable, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(Comparable value) throws Exception {
            return Pair.<Class, Predicate>pair(value.getClass(), Predicates.lessThan(value));
        }
    });
    public static final Parser<Pair<Class, Predicate>> TEXT_IS = TEXT_ONLY.map(new Callable1<String, Pair<Class, Predicate>>() {
        public Pair<Class, Predicate> call(String value) throws Exception {
            return Pair.<Class, Predicate>pair(String.class, Predicates.is(value));
        }
    });

    public static final Parser<Pair<Class, Predicate>> VALUE_PREDICATE = Parsers.or(GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS, GREATER_THAN, LESS_THAN, DATE_IS, TEXT_CONTAINS, TEXT_STARTS_WITH, TEXT_ENDS_WITH, TEXT_IS);

    public static final Parser<List<Pair<Class, Predicate>>> VALUE_PREDICATES = VALUE_PREDICATE.sepBy(ws(','));

    public static Parser<Predicate<Record>> VALUE_ONLY(final Sequence<? extends Keyword<?>> keywords) {
        return VALUE_PREDICATES.map(new Callable1<List<Pair<Class, Predicate>>, Predicate<Record>>() {
            public Predicate<Record> call(final List<Pair<Class, Predicate>> list) throws Exception {
                return or(keywords.map(new Callable1<Keyword<?>, Predicate<Record>>() {
                    public Predicate<Record> call(final Keyword<?> keyword) throws Exception {
                        return matchesValues(keyword.name(), list);
                    }
                }).toArray(Predicate.class));
            }
        });
    }


    private static Predicate<Record> matchesValues(final String name, List<Pair<Class, Predicate>> values) {
        return or(sequence(values).map(new Callable1<Pair<Class, Predicate>, Predicate<Record>>() {
            public Predicate<Record> call(Pair<Class, Predicate> pair) throws Exception {
                return where(keyword(name, pair.first()), pair.second());
            }
        }).toArray(Predicate.class));
    }

    public static Parser<Predicate<Record>> PARTS(final Sequence<? extends Keyword<?>> keywords) {
        return Parsers.or(NAME_AND_VALUE, VALUE_ONLY(keywords)).prefix(NEGATION);
    }

    public static Parser<Predicate<Record>> PARSER(final Sequence<? extends Keyword<?>> keywords) {
        return PARTS(keywords).infixl(OR.or(AND));
    }

    public static final Parser<Predicate<Record>> NAME_AND_VALUE = Parsers.tuple(NAME, ws(':').or(OPERATORS.peek()), VALUE_PREDICATES).map(new Callable1<Triple<String, Void, List<Pair<Class, Predicate>>>, Predicate<Record>>() {
        public Predicate<Record> call(Triple<String, Void, List<Pair<Class, Predicate>>> tuple) throws Exception {
            final String name = tuple.first();
            final List<Pair<Class, Predicate>> values = tuple.third();
            return matchesValues(name, values);
        }
    });


}
