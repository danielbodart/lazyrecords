package com.googlecode.lazyrecords.parser;

import com.googlecode.lazyparsec.Parser;
import com.googlecode.lazyparsec.Parsers;
import com.googlecode.lazyparsec.Scanners;
import com.googlecode.lazyparsec.pattern.CharacterPredicates;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.predicates.OrPredicate;
import com.googlecode.totallylazy.time.Seconds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.googlecode.lazyparsec.Scanners.isChar;
import static com.googlecode.lazyparsec.Scanners.notChar;
import static com.googlecode.lazyparsec.Scanners.pattern;
import static com.googlecode.lazyparsec.pattern.Patterns.regex;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Strings.contains;
import static com.googlecode.totallylazy.Strings.endsWith;
import static com.googlecode.totallylazy.Strings.startsWith;
import static java.util.regex.Pattern.quote;

@SuppressWarnings("unchecked")
public class Grammar {
    private final Sequence<? extends Keyword<?>> keywords;
    private final StringMappings mappings;
    private final Parser.Reference<Predicate<Record>> group = Parser.newReference();

    private Grammar(final Sequence<? extends Keyword<?>> keywords, StringMappings mappings) {
        this.keywords = keywords;
        this.mappings = mappings;
        group.set(PARTS.between(ws('('), ws(')')));
    }

    public static Parser<Predicate<Record>> PARSER(final Sequence<? extends Keyword<?>> keywords, final StringMappings mappings) {
        return new Grammar(keywords, mappings).PARTS;
    }

    public Parser<Predicate<Record>> VALUE_ONLY = VALUE_PREDICATES.map(new Callable1<List<Pair<String, Callable1<Object, Predicate>>>, Predicate<Record>>() {
            @Override
            public Predicate<Record> call(List<Pair<String, Callable1<Object, Predicate>>> pairs) throws Exception {
                List<Predicate<Record>> predicates = new ArrayList<Predicate<Record>>();
                for (final Keyword<?> keyword : keywords) {
                    predicates.add(toPredicate(keyword, pairs));
                }
                return OrPredicate.or(predicates);
            }
        });

    public final Parser<Predicate<Record>> GROUP = group.lazy();

    public final Parser<Predicate<Record>> NAME_AND_VALUE =  Parsers.tuple(NAME, ws(':').or(OPERATORS.peek()), VALUE_PREDICATES).map(new Callable1<Triple<String, Void, List<Pair<String, Callable1<Object, Predicate>>>>, Predicate<Record>>() {
        @Override
        public Predicate<Record> call(Triple<String, Void, List<Pair<String, Callable1<Object, Predicate>>>> triple) throws Exception {
            final String name = triple.first();
            final List<Pair<String, Callable1<Object, Predicate>>> values = triple.third();
            return toPredicate(Keyword.methods.matchKeyword(name, keywords), values);
        }
    });

    public Parser<Predicate<Record>> PARTS = Parsers.or(GROUP, NAME_AND_VALUE, VALUE_ONLY).prefix(NEGATION).infixl(OR.or(AND));

    private Predicate<Record> toPredicate(final Keyword<?> keyword, final List<Pair<String, Callable1<Object, Predicate>>> values) throws Exception {
        List<Predicate<Record>> valuesPredicates = new ArrayList<Predicate<Record>>();
        for (Pair<String, Callable1<Object, Predicate>> pair : values) {
            try {
                Object actualValue = mappings.toValue(keyword.forClass(), pair.first());
                Predicate<Record> where = where(keyword, pair.second().call(actualValue));
                valuesPredicates.add(where);
            } catch (Exception ignored) {
            }
        }
        return OrPredicate.or(valuesPredicates);
    }


    private static Parser<Void> ws(char value) {
        return ws(String.valueOf(value));
    }

    private static Parser<Void> ws(String value) {
        return pattern(regex(String.format("\\s*%s\\s*", quote(value))), value);
    }

    public static final Parser<String> DATE = pattern(regex("\\d{4}/\\d{1,2}/\\d{1,2}"), "date").source();
    public static final Parser<String> TEXT = isChar(CharacterPredicates.IS_ALPHA_NUMERIC_).many1().source();
    public static final Parser<String> QUOTED_TEXT = notChar('"').many1().source().between(isChar('"'), isChar('"'));
    public static final Parser<String> NULL = Scanners.string("null").retn(null);
    public static final Parser<String> TEXT_ONLY = Parsers.or(QUOTED_TEXT, TEXT);
    public static final Parser<String> VALUES = Parsers.or(DATE, NULL, TEXT_ONLY);
    public static final Parser<String> NAME = TEXT_ONLY;
    public static final Parser<Void> WILDCARD = isChar('*');
    public static final Parser<Void> GT = ws('>');
    public static final Parser<Void> GTE = ws(">=");
    public static final Parser<Void> LT = ws('<');
    public static final Parser<Void> LTE = ws("<=");
    public static final Parser<Void> OPERATORS = Parsers.or(GTE, GT, LTE, LT);

    public static final Parser<Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>> OR = ws("OR ").map(new Callable1<Void, Callable2<Predicate<Record>, Predicate<Record>, Predicate<Record>>>() {
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

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> TEXT_STARTS_WITH = TEXT.followedBy(WILDCARD).map(valueAndPredicateCreator(new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return startsWith(value.toString());
        }
    }));

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> TEXT_ENDS_WITH = Parsers.sequence(WILDCARD, TEXT).map(valueAndPredicateCreator(new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return endsWith(value.toString());
        }
    }));

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> TEXT_CONTAINS = TEXT.between(WILDCARD, WILDCARD).map(valueAndPredicateCreator(new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return contains(value.toString());
        }
    }));

    private static Parser<Callable1<Predicate<Record>, Predicate<Record>>> NEGATION = ws('-').or(ws("NOT")).optional().map(new Callable1<Void, Callable1<Predicate<Record>, Predicate<Record>>>() {
        public Callable1<Predicate<Record>, Predicate<Record>> call(Void aVoid) throws Exception {
            return new Callable1<Predicate<Record>, Predicate<Record>>() {
                public Predicate<Record> call(Predicate<Record> predicate) throws Exception {
                    return Predicates.not(predicate);
                }
            };
        }
    });

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> DATE_IS = DATE.map(valueAndPredicateCreator(new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object o) throws Exception {
            Date dateWithoutTime = (Date) o;
            Date upper = Seconds.add(dateWithoutTime, (24 * 60 * 60) - 1);
            return Predicates.between(dateWithoutTime, upper);
        }
    }));

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> GREATER_THAN = operator(GT, new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return Predicates.greaterThan((Comparable) value);
        }
    });

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> GREATER_THAN_OR_EQUALS = operator(GTE, new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return Predicates.greaterThanOrEqualTo((Comparable) value);
        }
    });

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> LESS_THAN_OR_EQUALS = operator(LTE, new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return Predicates.lessThanOrEqualTo((Comparable) value);
        }
    });

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> LESS_THAN = operator(LT, new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return Predicates.lessThan((Comparable) value);
        }
    });

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> IS_NULL = NULL.map(valueAndPredicateCreator(new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return Predicates.nullValue();
        }
    }));

    public static final Parser<Pair<String, Callable1<Object, Predicate>>> TEXT_IS = TEXT_ONLY.map(valueAndPredicateCreator(new Callable1<Object, Predicate>() {
        @Override
        public Predicate call(Object value) throws Exception {
            return Predicates.is(value);
        }
    }));

    private static Parser<Pair<String, Callable1<Object, Predicate>>> operator(Parser<Void> type, Callable1<Object, Predicate> predicateCreator) {
        return Parsers.sequence(type, VALUES).map(valueAndPredicateCreator(predicateCreator));
    }

    private static Callable1<String, Pair<String, Callable1<Object, Predicate>>> valueAndPredicateCreator(final Callable1<Object, Predicate> predicateCreator) {
        return new Callable1<String, Pair<String, Callable1<Object, Predicate>>>() {
            @Override
            public Pair<String, Callable1<Object, Predicate>> call(String value) throws Exception {
                return Pair.pair(value, predicateCreator);
            }
        };
    }

    public static Parser<Pair<String, Callable1<Object, Predicate>>> VALUE_PREDICATE = Parsers.or(GREATER_THAN_OR_EQUALS, LESS_THAN_OR_EQUALS, GREATER_THAN, LESS_THAN, DATE_IS, TEXT_CONTAINS, TEXT_STARTS_WITH, TEXT_ENDS_WITH, IS_NULL, TEXT_IS);

    public static Parser<List<Pair<String, Callable1<Object, Predicate>>>> VALUE_PREDICATES = VALUE_PREDICATE.sepBy(ws(','));
}
