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
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Lazy;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.Unchecked;

import java.util.Comparator;
import java.util.concurrent.Callable;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.sql.expressions.SetQuantifier.ALL;
import static com.googlecode.lazyrecords.sql.expressions.SetQuantifier.DISTINCT;
import static com.googlecode.totallylazy.Predicates.and;
import static com.googlecode.totallylazy.Sequences.join;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;

public class SelectBuilder implements Expressible, Callable<Expression>, Expression {
    public static final Keyword<Object> STAR = keyword("*");
    private final SqlGrammar grammar;
    private final SetQuantifier setQuantifier;
    private final Sequence<Keyword<?>> select;
    private final Definition table;
    private final Option<Predicate<? super Record>> where;
    private final Option<Comparator<? super Record>> comparator;
    private final Sequence<Join> joins;
    private final Lazy<Expression> value;

    private SelectBuilder(SqlGrammar grammar, SetQuantifier setQuantifier, Sequence<Keyword<?>> select, Definition table, Option<Predicate<? super Record>> where, Option<Comparator<? super Record>> comparator, Iterable<? extends Join> joins) {
        this.grammar = grammar;
        this.setQuantifier = setQuantifier;
        this.joins = sequence(joins);
        this.select = select.isEmpty() ? table.fields() : select;
        this.table = table;
        this.where = where;
        this.comparator = comparator;
        value = lazyExpression(this);
    }

    private Lazy<Expression> lazyExpression(final SelectBuilder builder) {
        return new Lazy<Expression>() {
            @Override
            protected Expression get() throws Exception {
                return builder.grammar.selectExpression(builder.table, builder.select, builder.setQuantifier, builder.where, builder.comparator, builder.joins);
            }
        };
    }

    public Expression call() throws Exception {
        return build();
    }

    public Expression express() {
        return build();
    }

    public Expression build() {
        return value.value();
    }

    @Override
    public String toString() {
        return build().toString();
    }

    public Sequence<Keyword<?>> select() {
        return select;
    }

    public static SelectBuilder from(SqlGrammar grammar, Definition table) {
        return new SelectBuilder(grammar, ALL, table.fields(), table, Option.<Predicate<? super Record>>none(), Option.<Comparator<? super Record>>none(), Option.<Join>none());
    }

    public SelectBuilder select(Keyword<?>... columns) {
        return select(sequence(columns));
    }

    public SelectBuilder select(Sequence<Keyword<?>> columns) {
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
        return new SelectBuilder(grammar, setQuantifier, qualifiedColumns, table, where, comparator, joins);
    }

    public SelectBuilder where(Predicate<? super Record> predicate) {
        Predicate<? super Record> newWhere = combineWithWhereClause(predicate);
        return new SelectBuilder(grammar, setQuantifier, select, table, Option.<Predicate<? super Record>>some(newWhere), comparator, joins);
    }

    private Predicate<? super Record> combineWithWhereClause(Predicate<? super Record> predicate) {
        if (where.isEmpty())
            return predicate;
        return and(where.get(), predicate);
    }

    public SelectBuilder orderBy(Comparator<? super Record> comparator) {
        return new SelectBuilder(grammar, setQuantifier, select, table, where, Option.<Comparator<? super Record>>some(comparator), joins);
    }

    public SelectBuilder count() {
        Aggregate<?, Number> recordCount = Aggregate.count(keyword("*", Long.class)).as("record_count");
        Sequence<Keyword<?>> sequence = Sequences.<Keyword<?>>sequence(recordCount);
        return new SelectBuilder(grammar, setQuantifier, sequence, table, where, Option.<Comparator<? super Record>>none(), joins);
    }

    public SelectBuilder distinct() {
        return new SelectBuilder(grammar, DISTINCT, select, table, where, comparator, joins);
    }

    public SelectBuilder reduce(Callable2<?, ?, ?> callable) {
        return select(aggregates(callable));
    }

    private Sequence<Keyword<?>> aggregates(Callable2<?, ?, ?> callable) {
        if (callable instanceof Aggregates) {
            Aggregates aggregates = (Aggregates) callable;
            return aggregates.value().unsafeCast();
        }
        Keyword<Object> cast = column();
        Aggregate<Object, Object> aggregate = Aggregate.aggregate(Unchecked.<Reducer<Object, Object>>cast(callable), cast, cast.forClass());
        return Sequences.<Keyword<?>>sequence(aggregate);
    }

    private Keyword<Object> column() {
        Sequence<Keyword<?>> columns = select();
        if (columns.size() == 1) return cast(columns.head());
        return cast(keyword("*", Long.class));
    }

    public SelectBuilder join(Join join) {
        return new SelectBuilder(grammar, setQuantifier, select.join(qualifyTable(joins.size(), join)).unique(), table, where, comparator, joins.add(join));
    }

    private static Sequence<Keyword<?>> qualifyTable(Number index, Join join) {
        SelectBuilder selectBuilder = SelectBuilder.selectBuilder(join);
        return selectBuilder.select().map(qualify(index));
    }

    private static Mapper<Keyword<?>, Keyword<?>> qualify(final Number index) {
        return new Mapper<Keyword<?>, Keyword<?>>() {
            @Override
            @SuppressWarnings("unchecked")
            public Keyword<?> call(Keyword<?> keyword) throws Exception {
                if( keyword instanceof AliasedKeyword) {
                    Keyword<?> source = ((AliasedKeyword<?>) keyword).source();
                    return new AliasedKeyword(source.metadata(Keywords.qualifier, SelectExpression.tableAlias(index)), keyword.name());
                }
                return keyword.metadata(Keywords.qualifier, SelectExpression.tableAlias(index));
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
        return (SelectBuilder) records.express();
    }
}
