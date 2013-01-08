package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.*;

import java.util.Comparator;
import java.util.concurrent.Callable;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.sql.expressions.SetQuantifier.ALL;
import static com.googlecode.lazyrecords.sql.expressions.SetQuantifier.DISTINCT;
import static com.googlecode.totallylazy.Predicates.and;
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
    private final Option<? extends Join> join;
    private final Value<Expression> value;

    private SelectBuilder(SqlGrammar grammar, SetQuantifier setQuantifier, Sequence<Keyword<?>> select, Definition table, Option<Predicate<? super Record>> where, Option<Comparator<? super Record>> comparator, Option<? extends Join> join) {
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
		Predicate<? super Record> newWhere = combineWithWhereClause(predicate);
		return new SelectBuilder(grammar, setQuantifier, select, table, Option.<Predicate<? super Record>>some(newWhere), comparator, join);
    }

	private Predicate<? super Record> combineWithWhereClause(Predicate<? super Record> predicate) {
		if(where.isEmpty())
			return predicate;
		return and(where.get(), predicate);
	}

	public SelectBuilder orderBy(Comparator<? super Record> comparator) {
        return new SelectBuilder(grammar, setQuantifier, select, table, where, Option.<Comparator<? super Record>>some(comparator), join);
    }

    public SelectBuilder count() {
        Aggregate<?, Number> recordCount = Aggregate.count(keyword("*", Long.class)).as("record_count");
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
        Keyword<Object> cast = column();
        Aggregate<Object, Object> aggregate = Aggregate.aggregate(Unchecked.<Reducer<Object,Object>>cast(callable), cast, cast.forClass());
        return Sequences.<Keyword<?>>sequence(aggregate);
    }

    private Keyword<Object> column() {
        Sequence<Keyword<?>> columns = select();
        if(columns.size() == 1 ) return cast(columns.head());
        return cast(keyword("*", Long.class)) ;
    }

    public SelectBuilder join(Option<Join> join) {
        return new SelectBuilder(grammar, setQuantifier, select.join(joinedKeywords(join)).unique().realise(), table, where, comparator, join);
    }

    private static Sequence<Keyword<?>> joinedKeywords(Option<? extends Join> join) {
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
