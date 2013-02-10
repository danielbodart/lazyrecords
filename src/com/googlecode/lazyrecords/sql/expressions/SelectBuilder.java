package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Aggregate;
import com.googlecode.lazyrecords.Aggregates;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.Unchecked;

import java.util.Comparator;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSetQuantifier.ALL;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSetQuantifier.DISTINCT;
import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.totallylazy.Sequences.join;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class SelectBuilder implements Expression, ExpressionBuilder {
    private final SqlGrammar grammar;
    private final SetQuantifier setQuantifier;
    private final Sequence<? extends Keyword<?>> select;
    private final Definition table;
    private final Option<Predicate<? super Record>> where;
    private final Option<Comparator<? super Record>> comparator;
    private final Lazy<SelectExpression> value;

    private SelectBuilder(SqlGrammar grammar, SetQuantifier setQuantifier, Sequence<? extends Keyword<?>> select,
                          Definition table, Option<Predicate<? super Record>> where, Option<Comparator<? super Record>> comparator) {
        this.grammar = grammar;
        this.setQuantifier = setQuantifier;
        this.select = select.isEmpty() ? table.fields() : select;
        this.table = table;
        this.where = where;
        this.comparator = comparator;
        value = lazyExpression();
    }

    private Lazy<SelectExpression> lazyExpression() {
        return new Lazy<SelectExpression>() {
            @Override
            protected SelectExpression get() throws Exception {
                return grammar.selectExpression(Option.<SetQuantifier>some(setQuantifier),
                        select,
                        table,
                        where,
                        comparator);
            }
        };
    }

    public SelectExpression build() {
        return value.value();
    }

    @Override
    public String toString() {
        return build().toString();
    }

    @Override
    public Sequence<Keyword<?>> fields() {
        return select.unsafeCast();
    }

    public static SelectBuilder from(SqlGrammar grammar, Definition table) {
        return new SelectBuilder(grammar, ALL, table.fields(), table, Option.<Predicate<? super Record>>none(), Option.<Comparator<? super Record>>none());
    }

    @Override
    public SelectBuilder select(Keyword<?>... columns) {
        return select(sequence(columns));
    }

    @Override
    public SelectBuilder select(Sequence<? extends Keyword<?>> columns) {
        Sequence<? extends Keyword<?>> qualifiedColumns = table.metadata(Keywords.alias).fold(columns, new Function2<Sequence<? extends Keyword<?>>, String, Sequence<Keyword<?>>>() {
            @Override
            public Sequence<Keyword<?>> call(Sequence<? extends Keyword<?>> keywords, final String alias) throws Exception {
                return keywords.map(new UnaryFunction<Keyword<?>>() {
                    @Override
                    public Keyword<?> call(Keyword<?> keyword) throws Exception {
                        return keyword.metadata(Keywords.qualifier, alias);
                    }
                });
            }
        });
        return new SelectBuilder(grammar, setQuantifier, qualifiedColumns, table, where, comparator);
    }

    @Override
    public SelectBuilder filter(Predicate<? super Record> predicate) {
        return new SelectBuilder(grammar, setQuantifier, select, table, Option.<Predicate<? super Record>>some(combine(where, predicate)), comparator);
    }

    @Override
    public SelectBuilder orderBy(Comparator<? super Record> comparator) {
        return new SelectBuilder(grammar, setQuantifier, select, table, where, Option.<Comparator<? super Record>>some(comparator));
    }

    @Override
    public SelectBuilder count() {
        return new SelectBuilder(grammar, setQuantifier, countStar(), table, where, Option.<Comparator<? super Record>>none());
    }

    @Override
    public SelectBuilder distinct() {
        return new SelectBuilder(grammar, DISTINCT, select, table, where, comparator);
    }

    @Override
    public SelectBuilder reduce(Reducer<?,?> reducer) {
        return select(aggregates(reducer, fields()));
    }

    @Override
    public String text() {
        return build().text();
    }

    @Override
    public Sequence<Object> parameters() {
        return build().parameters();
    }

    public Definition table() {
        return table;
    }

    public static Predicate<? super Record> combine(final Option<Predicate<? super Record>> previous, Predicate<? super Record> predicate) {
        if (previous.isEmpty()) return predicate;
        return and(previous.get(), predicate);
    }

    public static Sequence<Keyword<?>> countStar() {
        Aggregate<?, Number> recordCount = Aggregate.count(keyword("*", Long.class)).as("record_count");
        return Sequences.<Keyword<?>>sequence(recordCount);
    }

    public static Sequence<Keyword<?>> aggregates(Reducer<?,?> callable, Sequence<Keyword<?>> fields) {
        if (callable instanceof Aggregates) {
            Aggregates aggregates = (Aggregates) callable;
            return aggregates.value().unsafeCast();
        }
        Keyword<Object> cast = column(fields);
        Aggregate<Object, Object> aggregate = Aggregate.aggregate(Unchecked.<Reducer<Object, Object>>cast(callable), cast, cast.forClass());
        return Sequences.<Keyword<?>>sequence(aggregate);
    }

    public static Keyword<Object> column(Sequence<Keyword<?>> fields) {
        if (fields.size() == 1) return cast(fields.head());
        return cast(keyword("*", Long.class));
    }


}
