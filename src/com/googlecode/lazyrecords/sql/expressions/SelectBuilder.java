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
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.totallylazy.Sequences.join;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class SelectBuilder implements Expression, ExpressionBuilder {
    private final SqlGrammar grammar;
    private final SetQuantifier setQuantifier;
    private final Sequence<Keyword<?>> select;
    private final Definition table;
    private final Option<Predicate<? super Record>> where;
    private final Option<Comparator<? super Record>> comparator;
    private final Lazy<SelectExpression> value;

    private SelectBuilder(SqlGrammar grammar, SetQuantifier setQuantifier, Sequence<Keyword<?>> select,
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
                        grammar.selectList(select),
                        grammar.fromClause(table),
                        grammar.whereClause(where),
                        grammar.orderByClause(comparator));
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
        return select;
    }

    public static ExpressionBuilder from(SqlGrammar grammar, Definition table) {
        return new SelectBuilder(grammar, ALL, table.fields(), table, Option.<Predicate<? super Record>>none(), Option.<Comparator<? super Record>>none());
    }

    @Override
    public ExpressionBuilder select(Keyword<?>... columns) {
        return select(sequence(columns));
    }

    @Override
    public ExpressionBuilder select(Sequence<Keyword<?>> columns) {
        Sequence<Keyword<?>> qualifiedColumns = table.metadata(Keywords.alias).fold(columns, new Function2<Sequence<Keyword<?>>, String, Sequence<Keyword<?>>>() {
            @Override
            public Sequence<Keyword<?>> call(Sequence<Keyword<?>> keywords, final String alias) throws Exception {
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
    public ExpressionBuilder where(Predicate<? super Record> predicate) {
        Predicate<? super Record> newWhere = combineWithWhereClause(predicate);
        return new SelectBuilder(grammar, setQuantifier, select, table, Option.<Predicate<? super Record>>some(newWhere), comparator);
    }

    private Predicate<? super Record> combineWithWhereClause(Predicate<? super Record> predicate) {
        if (where.isEmpty()) return predicate;
        return and(where.get(), predicate);
    }

    @Override
    public ExpressionBuilder orderBy(Comparator<? super Record> comparator) {
        return new SelectBuilder(grammar, setQuantifier, select, table, where, Option.<Comparator<? super Record>>some(comparator));
    }

    @Override
    public ExpressionBuilder count() {
        Aggregate<?, Number> recordCount = Aggregate.count(keyword("*", Long.class)).as("record_count");
        Sequence<Keyword<?>> sequence = Sequences.<Keyword<?>>sequence(recordCount);
        return new SelectBuilder(grammar, setQuantifier, sequence, table, where, Option.<Comparator<? super Record>>none());
    }

    @Override
    public ExpressionBuilder distinct() {
        return new SelectBuilder(grammar, DISTINCT, select, table, where, comparator);
    }

    @Override
    public ExpressionBuilder reduce(Reducer<?,?> reducer) {
        return select(aggregates(reducer));
    }

    private Sequence<Keyword<?>> aggregates(Reducer<?,?> callable) {
        if (callable instanceof Aggregates) {
            Aggregates aggregates = (Aggregates) callable;
            return aggregates.value().unsafeCast();
        }
        Keyword<Object> cast = column();
        Aggregate<Object, Object> aggregate = Aggregate.aggregate(Unchecked.<Reducer<Object, Object>>cast(callable), cast, cast.forClass());
        return Sequences.<Keyword<?>>sequence(aggregate);
    }

    private Keyword<Object> column() {
        Sequence<Keyword<?>> columns = fields();
        if (columns.size() == 1) return cast(columns.head());
        return cast(keyword("*", Long.class));
    }

    private static Sequence<Keyword<?>> qualifyTable(Number index, Join join) {
        SelectBuilder selectBuilder = SelectBuilder.selectBuilder(join);
        return selectBuilder.fields().map(qualify(index));
    }

    private static Mapper<Keyword<?>, Keyword<?>> qualify(final Number index) {
        return new Mapper<Keyword<?>, Keyword<?>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Keyword<?> call(Keyword<?> keyword) throws Exception {
                if( keyword instanceof AliasedKeyword) {
                    Keyword<?> source = ((AliasedKeyword<?>) keyword).source();
                    return new AliasedKeyword(source.metadata(Keywords.qualifier, AnsiSelectExpression.tableAlias(index)), keyword.name());
                }
                return keyword.metadata(Keywords.qualifier, AnsiSelectExpression.tableAlias(index));
            }
        };
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

    public static SelectBuilder selectBuilder(Join join) {
        Expressible records = (Expressible) join.records();
        return (SelectBuilder) records.build();
    }
}
