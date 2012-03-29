package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Named;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.lucene.mappings.LuceneMappings;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.comparators.AscendingComparator;
import com.googlecode.totallylazy.comparators.DescendingComparator;
import com.googlecode.totallylazy.predicates.AllPredicate;
import com.googlecode.totallylazy.predicates.AndPredicate;
import com.googlecode.totallylazy.predicates.Between;
import com.googlecode.totallylazy.predicates.ContainsPredicate;
import com.googlecode.totallylazy.predicates.EndsWithPredicate;
import com.googlecode.totallylazy.predicates.EqualsPredicate;
import com.googlecode.totallylazy.predicates.GreaterThan;
import com.googlecode.totallylazy.predicates.GreaterThanOrEqualTo;
import com.googlecode.totallylazy.predicates.InPredicate;
import com.googlecode.totallylazy.predicates.LessThan;
import com.googlecode.totallylazy.predicates.LessThanOrEqualTo;
import com.googlecode.totallylazy.predicates.Not;
import com.googlecode.totallylazy.predicates.NotEqualsPredicate;
import com.googlecode.totallylazy.predicates.NotNullPredicate;
import com.googlecode.totallylazy.predicates.NullPredicate;
import com.googlecode.totallylazy.predicates.OrPredicate;
import com.googlecode.totallylazy.predicates.StartsWithPredicate;
import com.googlecode.totallylazy.predicates.WherePredicate;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;

import java.util.Comparator;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Lucene {
    public static final Keyword<String> RECORD_KEY = keyword("type", String.class);

    public static TermQuery record(Definition definition) {
        return new TermQuery(new Term(RECORD_KEY.toString(), definition.name()));
    }

    public static Query and(Query... queries) {
        return and(sequence(queries));
    }

    public static Query and(Iterable<Query> queries) {
        return sequence(queries).fold(new BooleanQuery(), add(BooleanClause.Occur.MUST));
    }

    public static Query or(Query... queries) {
        return or(sequence(queries));
    }

    public static Query or(Iterable<Query> queries) {
        return sequence(queries).fold(new BooleanQuery(), add(BooleanClause.Occur.SHOULD));
    }

    public static Query not(Query... queries) {
        return not(sequence(queries));
    }

    public static Query not(Iterable<Query> queries) {
        return sequence(queries).fold(new BooleanQuery(), add(BooleanClause.Occur.MUST_NOT));
    }

    private static Function2<? super BooleanQuery, ? super Query, BooleanQuery> add(final BooleanClause.Occur occur) {
        return new Function2<BooleanQuery, Query, BooleanQuery>() {
            public BooleanQuery call(BooleanQuery booleanQuery, Query query) throws Exception {
                // FIX Lucene issue where it does not understand nested boolean negatives
                if (query instanceof BooleanQuery && occur.equals(BooleanClause.Occur.MUST)) {
                    BooleanClause[] clauses = ((BooleanQuery) query).getClauses();
                    if (clauses.length == 1 && clauses[0].getOccur().equals(BooleanClause.Occur.MUST_NOT)) {
                        booleanQuery.add(clauses[0]);
                        return booleanQuery;
                    }
                }
                booleanQuery.add(query, occur);
                return booleanQuery;
            }
        };
    }

    private final StringMappings mappings;

    public Lucene(StringMappings mappings) {
        this.mappings = mappings;
    }

    public static Sort sort(Comparator<? super Record> comparator) {
        if (comparator instanceof AscendingComparator) {
            return sortBy(name(((AscendingComparator) comparator).callable()), false);
        }
        if (comparator instanceof DescendingComparator) {
            return sortBy(name(((DescendingComparator) comparator).callable()), true);
        }
        throw new UnsupportedOperationException("Unsupported comparator " + comparator);
    }

    private static Sort sortBy(String name, boolean reverse) {
        return new Sort(new SortField(name, SortField.STRING, reverse));
    }

    private static String name(Callable1<?, ?> callable) {
        if(callable instanceof Named){
            return ((Named) callable).name();
        }
        throw new UnsupportedOperationException("Unsupported callable " + callable);
    }

    public Query query(Predicate<? super Record> predicate) {
        if (predicate instanceof WherePredicate) {
            return where(Unchecked.<WherePredicate<Record, ?>>cast(predicate));
        }
        if (predicate instanceof AndPredicate) {
            return and(Unchecked.<AndPredicate<Record>>cast(predicate).predicates().map(asQuery()));
        }
        if (predicate instanceof OrPredicate) {
            return or(Unchecked.<OrPredicate<Record>>cast(predicate).predicates().map(asQuery()));
        }
        if (predicate instanceof Not) {
            return not(query(Unchecked.<Not<Record>>cast(predicate).predicate()));
        }
        if (predicate instanceof AllPredicate) {
            return new MatchAllDocsQuery();
        }
        throw new UnsupportedOperationException();
    }

    private Function1<Predicate<? super Record>, Query> asQuery() {
        return new Function1<Predicate<? super Record>, Query>() {
            public Query call(Predicate<? super Record> predicate) throws Exception {
                return query(predicate);
            }
        };
    }

    public Query where(WherePredicate<Record, ?> where) {
        Keyword<?> keyword = (Keyword<?>) where.callable();
        Predicate<?> predicate = where.predicate();
        return query(keyword, predicate);
    }

    private Query query(Keyword<?> keyword, Predicate<?> predicate) {
        if (predicate instanceof EqualsPredicate) {
            return equalTo(keyword, ((Value) predicate).value());
        }

        if (predicate instanceof GreaterThan) {
            return greaterThan(keyword, ((Value) predicate).value());
        }

        if (predicate instanceof GreaterThanOrEqualTo) {
            return greaterThanOrEqual(keyword, ((Value) predicate).value());
        }

        if (predicate instanceof LessThan) {
            return lessThan(keyword, ((Value) predicate).value());
        }

        if (predicate instanceof LessThanOrEqualTo) {
            return lessThanOrEqual(keyword, ((Value) predicate).value());
        }
        if (predicate instanceof Between) {
            Object lower = ((Between) predicate).lower();
            Object upper = ((Between) predicate).upper();
            return between(keyword, lower, upper);
        }
        if (predicate instanceof NotEqualsPredicate) {
            return not(equalTo(keyword, ((Value) predicate).value()));
        }
        if (predicate instanceof Not) {
            Predicate p = ((Not) predicate).predicate();
            return not(query(keyword, p));
        }
        if (predicate instanceof InPredicate) {
            Sequence<?> values = ((InPredicate<?>) predicate).values();
            return or(values.map(asQuery(keyword)));
        }
        if (predicate instanceof StartsWithPredicate) {
            String value = ((StartsWithPredicate) predicate).value();
            return new PrefixQuery(new Term(keyword.toString(), value));
        }
        if (predicate instanceof ContainsPredicate) {
            String value = ((ContainsPredicate) predicate).value();
            return new WildcardQuery(new Term(keyword.toString(), "*" + value + "*"));
        }
        if (predicate instanceof EndsWithPredicate) {
            String value = ((EndsWithPredicate) predicate).value();
            return new WildcardQuery(new Term(keyword.toString(), "*" + value));
        }
        if (predicate instanceof NotNullPredicate) {
            return notNull(keyword);
        }
        if (predicate instanceof NullPredicate) {
            return not(notNull(keyword));
        }
        throw new UnsupportedOperationException();
    }

    private Query newRange(Keyword<?> keyword, Object lower, Object upper, boolean minInclusive, boolean maxInclusive) {
        return new TermRangeQuery(keyword.name(), lower == null ? null : mappings.toString(keyword.forClass(), lower), upper == null ? null : mappings.toString(keyword.forClass(), upper), minInclusive, maxInclusive);
    }

    private Query equalTo(Keyword<?> keyword, Object value) {
        return newRange(keyword, value, value, true, true);
    }

    private Query greaterThan(Keyword<?> keyword, Object value) {
        return newRange(keyword, value, null, false, true);
    }

    private Query greaterThanOrEqual(Keyword<?> keyword, Object value) {
        return newRange(keyword, value, null, true, true);
    }

    private Query lessThan(Keyword<?> keyword, Object value) {
        return newRange(keyword, null, value, true, false);
    }

    private Query lessThanOrEqual(Keyword<?> keyword, Object value) {
        return newRange(keyword, null, value, true, true);
    }

    private Query between(Keyword<?> keyword, Object lower, Object upper) {
        return newRange(keyword, lower, upper, true, true);
    }

    private Query notNull(Keyword<?> keyword) {
        return newRange(keyword, null, null, true, true);
    }

    private Function1<Object, Query> asQuery(final Keyword<?> keyword) {
        return new Function1<Object, Query>() {
            public Query call(Object o) throws Exception {
                return equalTo(keyword, o);
            }
        };
    }


}
