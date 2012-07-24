package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Aggregate;
import com.googlecode.lazyrecords.Aggregates;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.callables.CountNotNull;

import java.util.Comparator;
import java.util.concurrent.Callable;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.sql.expressions.SetQuantifier.ALL;
import static com.googlecode.lazyrecords.sql.expressions.SetQuantifier.DISTINCT;
import static com.googlecode.totallylazy.Sequences.sequence;

public class SelectBuilder implements Expressible, Callable<Expression>, Expression {
    public static final Keyword<Object> STAR = keyword("*");
    private final SqlGrammar grammar;
    private final SetQuantifier setQuantifier;
    private final Sequence<Keyword<?>> select;
    private final Definition table;
    private final Option<Predicate<? super Record>> where;
    private final Option<Comparator<? super Record>> comparator;
    private final Option<Join> join;
    private final Value<Expression> value;

    private SelectBuilder(SqlGrammar grammar, SetQuantifier setQuantifier, Sequence<Keyword<?>> select, Definition table, Option<Predicate<? super Record>> where, Option<Comparator<? super Record>> comparator, Option<Join> join) {
        this.grammar = grammar;
        this.setQuantifier = setQuantifier;
        this.join = join;
        this.select = select.isEmpty() ? table.fields() : select;
        this.table = table;
        this.where = where;
        this.comparator = comparator;
        value = lazyExpression(this);
    }

    private Function<Expression> lazyExpression(final SelectBuilder builder) {
        return new Function<Expression>() {
            @Override
            public Expression call() throws Exception {
                return builder.grammar.selectExpression(builder.table, builder.select, builder.setQuantifier, builder.where, builder.comparator, builder.join);
            }
        }.lazy();
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
        return new SelectBuilder(grammar, setQuantifier, columns, table, where, comparator, join);
    }

    public SelectBuilder where(Predicate<? super Record> predicate) {
        return new SelectBuilder(grammar, setQuantifier, select, table, Option.<Predicate<? super Record>>some(predicate), comparator, join);
    }

    public SelectBuilder orderBy(Comparator<? super Record> comparator) {
        return new SelectBuilder(grammar, setQuantifier, select, table, where, Option.<Comparator<? super Record>>some(comparator), join);
    }

    public SelectBuilder count() {
        Aggregate<Long, Number> recordCount = Aggregate.aggregate(CountNotNull.count(), keyword("*", Long.class)).as("record_count");
        Sequence<Keyword<?>> sequence = Sequences.<Keyword<?>>sequence(recordCount);
        return new SelectBuilder(grammar, setQuantifier, sequence, table, where, Option.<Comparator<? super Record>>none(), join);
    }

    public SelectBuilder distinct() {
        return new SelectBuilder(grammar, DISTINCT, select, table, where, comparator, join);
    }

    public SelectBuilder reduce(Callable2<?, ?, ?> callable) {
        return select(aggregates(callable));
    }

    private Sequence<Keyword<?>> aggregates(Callable2<?, ?, ?> callable) {
        if (callable instanceof Aggregates) {
            Aggregates aggregates = (Aggregates) callable;
            return aggregates.value().unsafeCast();
        }
        Keyword<?> head = select().head();
        Aggregate<Object, Object> aggregate = Aggregate.aggregate(Unchecked.<Callable2<Object, Object, Object>>cast(callable), Unchecked.<Keyword<Object>>cast(head));
        return Sequences.<Keyword<?>>sequence(aggregate);
    }

    public SelectBuilder join(Option<Join> join) {
        return new SelectBuilder(grammar, setQuantifier, select.join(joinedKeywords(join)).unique().realise(), table, where, comparator, join);
    }

    private static Sequence<Keyword<?>> joinedKeywords(Option<Join> join) {
        return join.toSequence().flatMap(new Function1<Join, Sequence<Keyword<?>>>() {
            @Override
            public Sequence<Keyword<?>> call(Join join) throws Exception {
                Expressible records = (Expressible) join.records();
                SelectBuilder select = (SelectBuilder) records.express();
                return select.select();
            }
        });
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
}
