package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.predicates.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Lucene {
    public static final Keyword<String> RECORD_KEY = keyword("type", String.class);
    static final Sort NO_SORT = new Sort();

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

    public Query query(Predicate<? super Record> predicate) { return new multi() {}.<Query>methodOption(predicate).getOrThrow(new UnsupportedOperationException()); }
    public Query query(WherePredicate<Record, ?> wherePredicate) { return where(wherePredicate); }
    public Query query(AndPredicate<Record> andPredicate) { return and(andPredicate.predicates().map(asQuery())); }
    public Query query(OrPredicate<Record> orPredicate) { return or(orPredicate.predicates().map(asQuery())); }
    public Query query(Not<Record> notPredicate) { return not(query(notPredicate.predicate())); }
    public Query query(AllPredicate allPredicate) { return new MatchAllDocsQuery(); }

    public Query query(Keyword<?> keyword, Predicate<?> predicate) { return new multi(){}.<Query>methodOption(keyword, predicate).getOrThrow(new UnsupportedOperationException()); }
    public Query query(Keyword<?> keyword, EqualsPredicate<?> predicate) { return equalTo(keyword, predicate.value()); }
    public Query query(Keyword<?> keyword, GreaterThan<?> predicate) { return greaterThan(keyword, predicate.value()); }
    public Query query(Keyword<?> keyword, GreaterThanOrEqualTo<?> predicate) { return greaterThanOrEqual(keyword, predicate.value()); }
    public Query query(Keyword<?> keyword, LessThan<?> predicate) { return lessThan(keyword, predicate.value()); }
    public Query query(Keyword<?> keyword, LessThanOrEqualTo<?> predicate) { return lessThanOrEqual(keyword, predicate.value()); }
    public Query query(Keyword<?> keyword, Between<?> predicate) { return between(keyword, predicate.lower(), predicate.upper()); }
    public Query query(Keyword<?> keyword, NotEqualsPredicate<?> predicate) { return not(equalTo(keyword, predicate.value())); }
    public Query query(Keyword<?> keyword, Not<?> predicate) { return not(query(keyword, predicate.predicate())); }
    public Query query(Keyword<?> keyword, InPredicate<?> predicate) { return or(sequence(predicate.values()).map(asQuery(keyword))); }
    public Query query(Keyword<?> keyword, StartsWithPredicate predicate) { return new PrefixQuery(new Term(keyword.toString(), predicate.value())); }
    public Query query(Keyword<?> keyword, ContainsPredicate predicate) { return new WildcardQuery(new Term(keyword.toString(), "*" + predicate.value() + "*")); }
    public Query query(Keyword<?> keyword, EndsWithPredicate predicate) { return new WildcardQuery(new Term(keyword.toString(), "*" + predicate.value())); }
    public Query query(Keyword<?> keyword, NotNullPredicate<?> predicate) { return notNull(keyword); }
    public Query query(Keyword<?> keyword, NullPredicate<?> predicate) { return not(notNull(keyword)); }

    private Query newRange(Keyword<?> keyword, Object lower, Object upper, boolean minInclusive, boolean maxInclusive) {
        return new TermRangeQuery(keyword.name(), lower == null ? null : mappings.toString(keyword.forClass(), lower), upper == null ? null : mappings.toString(keyword.forClass(), upper), minInclusive, maxInclusive);
    }

    public Query where(WherePredicate<Record, ?> where) {
        Keyword<?> keyword = (Keyword<?>) where.callable();
        Predicate<?> predicate = where.predicate();
        return query(keyword, predicate);
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

    private Function1<Predicate<Record>, Query> asQuery() {
        return new Function1<Predicate<Record>, Query>() {
            public Query call(Predicate<Record> predicate) throws Exception {
                return query(predicate);
            }
        };
    }
}