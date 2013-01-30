package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Aggregate;
import com.googlecode.lazyrecords.Aliased;
import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.CompositeKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Binary;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.callables.JoinString;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.lazyrecords.sql.expressions.SetFunctionType.setFunctionType;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;

public class AnsiSelectList extends CompoundExpression implements SelectList {
    private final Sequence<DerivedColumn> derivedColumns;

    public AnsiSelectList(Sequence<DerivedColumn> derivedColumns) {
        super(derivedColumns, ", ");
        this.derivedColumns = derivedColumns;
    }

    public static SelectList selectList(SqlGrammar grammar, final Sequence<Keyword<?>> select) {
        return new AnsiSelectList(select.map(derivedColumn()));
    }

    public static Function1<Keyword<?>, DerivedColumn> derivedColumn() {
        return new Function1<Keyword<?>, DerivedColumn>() {
            public DerivedColumn call(Keyword<?> keyword) throws Exception {
                return derivedColumn(keyword);
            }
        };
    }

    public static <T> ValueExpression valueExpression(Callable1<? super Record, T> callable) {
        if (callable instanceof CompositeKeyword) {
            CompositeKeyword<?> composite = (CompositeKeyword<?>) callable;
            Binary<?> combiner = composite.combiner();
            if (combiner instanceof JoinString) {
                return new CompositeExpression(composite.keywords().map(name()), "(", "||", ")");
            }
            throw new UnsupportedOperationException("Unsupported combiner " + combiner);
        }
        if (callable instanceof Aggregate) {
            Aggregate aggregate = (Aggregate) callable;
            return setFunctionType(aggregate.reducer(), aggregate.source());
        }
        if (callable instanceof AliasedKeyword) {
            AliasedKeyword aliasedKeyword = (AliasedKeyword) callable;
            return name(aliasedKeyword.source());
        }
        if (callable instanceof Keyword) {
            Keyword<?> keyword = (Keyword) callable;
            return name(keyword);
        }
        throw new UnsupportedOperationException("Unsupported reducer " + callable);
    }

    public static <T> DerivedColumn derivedColumn(Callable1<? super Record, T> callable) {
        ValueExpression expression = valueExpression(callable);
        if (callable instanceof Aliased) {
            return AnsiDerivedColumn.derivedColumn(expression, some(asClause((Keyword<?>) callable)));
        }
        return AnsiDerivedColumn.derivedColumn(expression, none(AsClause.class));
    }

    public static AsClause asClause(Keyword<?> keyword) {
        return asClause(keyword.name());
    }

    public static AsClause asClause(String name) {
        return AnsiAsClause.asClause(name);
    }

    @Override
    public Sequence<DerivedColumn> derivedColumns() {
        return derivedColumns;
    }
}
