package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyparsec.Parser;
import com.googlecode.lazyparsec.Parsers;
import com.googlecode.lazyparsec.pattern.CharacterPredicates;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.OrPredicate;
import com.googlecode.totallylazy.time.Dates;
import com.googlecode.totallylazy.time.Seconds;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.googlecode.lazyparsec.Scanners.*;
import static com.googlecode.lazyparsec.pattern.Patterns.regex;
import static com.googlecode.lazyrecords.Record.methods.getKeyword;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.*;

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

    public static final Parser<Predicate> TEXT_STARTS_WITH = TEXT.followedBy(WILDCARD).map(new Callable1<String, Predicate>() {
        public Predicate call(String value) throws Exception {
            return startsWith(value);
        }
    });

    public static final Parser<Predicate> TEXT_ENDS_WITH = Parsers.sequence(WILDCARD, TEXT).map(new Callable1<String, Predicate>() {
        public Predicate call(String value) throws Exception {
            return endsWith(value);
        }
    });

    public static final Parser<Predicate> TEXT_CONTAINS = TEXT.between(WILDCARD, WILDCARD).map(new Callable1<String, Predicate>() {
        public Predicate call(String value) throws Exception {
            return contains(value);
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

    public static final Parser<Predicate> DATE_IS = DATE.map(new Callable1<Date, Predicate>() {
        public Predicate call(Date dateWithoutTime) throws Exception {
            Date upper = Seconds.add(dateWithoutTime, (24 * 60 * 60) - 1);
            return Predicates.between(dateWithoutTime, upper);
        }
    });

    public static final Parser<Predicate> GREATER_THAN = Parsers.sequence(GT, VALUES).map(new Callable1<Comparable, Predicate>() {
        public Predicate call(Comparable value) throws Exception {
            return Predicates.greaterThan(value);
        }
    });

    public static final Parser<Predicate> GREATER_THAN_OR_EQUALS = Parsers.sequence(GTE, VALUES).map(new Callable1<Comparable, Predicate>() {
        public Predicate call(Comparable value) throws Exception {
            return Predicates.greaterThanOrEqualTo(value);
        }
    });

    public static final Parser<Predicate> LESS_THAN_OR_EQUALS = Parsers.sequence(LTE, VALUES).map(new Callable1<Comparable, Predicate>() {
        public Predicate call(Comparable value) throws Exception {
            return Predicates.lessThanOrEqualTo(value);
        }
    });

    public static final Parser<Pair<Comparable, Callable1<Comparable, Predicate>>> LESS_THAN = Parsers.sequence(LT, VALUES).map(Grammar.<Comparable, Comparable>valueAndPredicateCreator(new Callable1<Comparable, Predicate>() {
        @Override
        public Predicate call(Comparable value) throws Exception {
            return Predicates.lessThan(value);
        }
    }));

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> TEXT_IS = TEXT_ONLY.map(Grammar.<String, Object>valueAndPredicateCreator(new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return Predicates.is(value);
        }
    }));

    private static <I, O> Callable1<I, Pair<I, Callable1<O, Predicate>>> valueAndPredicateCreator(final Callable1<O, Predicate> predicateCreator) {
        return new Callable1<I, Pair<I, Callable1<O, Predicate>>>() {
            @Override
            public Pair<I, Callable1<O, Predicate>> call(I value) throws Exception {
                return Pair.pair(value, predicateCreator);
            }
        };
    }

    public static Parser<Pair<String, Callable1<Object, Predicate>>> VALUE_PREDICATE = Parsers.or(TEXT_IS); //Parsers.or(GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS, GREATER_THAN, LESS_THAN, DATE_IS, TEXT_CONTAINS, TEXT_STARTS_WITH, TEXT_ENDS_WITH,

    public static Parser<List<Pair<String, Callable1<Object, Predicate>>>> VALUE_PREDICATES = VALUE_PREDICATE.sepBy(ws(','));

    public static Parser<Predicate<Record>> VALUE_ONLY(final Sequence<? extends Keyword<?>> keywords, final StringMappings mappings) {
        return VALUE_PREDICATES.map(new Callable1<List<Pair<String, Callable1<Object, Predicate>>>, Predicate<Record>>() {
            @Override
            public Predicate<Record> call(List<Pair<String, Callable1<Object, Predicate>>> pairs) throws Exception {
                List<Predicate<Record>> predicates = new ArrayList<Predicate<Record>>();
                for (final Keyword<?> keyword : keywords) {
                    predicates.add(toPredicate(mappings, keyword, pairs));
                }
                return OrPredicate.or(predicates);

            }
        });
    }

    public static Parser<Predicate<Record>> PARTS(final Sequence<? extends Keyword<?>> keywords, StringMappings mappings) {
        return Parsers.or(NAME_AND_VALUE(keywords, mappings), VALUE_ONLY(keywords, mappings)).prefix(NEGATION);
    }

    public static Parser<Predicate<Record>> PARSER(final Sequence<? extends Keyword<?>> keywords, StringMappings mappings) {
        return PARTS(keywords, mappings).infixl(OR.or(AND));
    }

    public static final Parser<Predicate<Record>> NAME_AND_VALUE(final Sequence<? extends Keyword<?>> keywords, final StringMappings mappings) {
        return Parsers.tuple(NAME, ws(':').or(OPERATORS.peek()), VALUE_PREDICATES).map(new Callable1<Triple<String, Void, List<Pair<String, Callable1<Object, Predicate>>>>, Predicate<Record>>() {
            @Override
            public Predicate<Record> call(Triple<String, Void, List<Pair<String, Callable1<Object, Predicate>>>> triple) throws Exception {
                final String name = triple.first();
                final List<Pair<String, Callable1<Object, Predicate>>> values = triple.third();
                return toPredicate(mappings, getKeyword(name, keywords), values);
            }
        });
    }

    private static Predicate<Record> toPredicate(final StringMappings mappings, final Keyword<?> keyword, final List<Pair<String, Callable1<Object, Predicate>>> values) throws Exception {
        final StringMapping<?> mapping = mappings.get(keyword.forClass());

        List<Predicate<Record>> valuesPredicates = new ArrayList<Predicate<Record>>();
        for (Pair<String, Callable1<Object, Predicate>> pair : values) {
            Object actualValue = mapping.toValue(pair.first());
            Predicate<Record> where = where(keyword, pair.second().call(actualValue));
            valuesPredicates.add(where);
        }
        return OrPredicate.or(valuesPredicates);
    }
}
