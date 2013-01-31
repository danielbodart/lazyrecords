package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.lazyrecords.sql.expressions.DerivedColumn;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.FromClause;
import com.googlecode.lazyrecords.sql.expressions.OrderByClause;
import com.googlecode.lazyrecords.sql.expressions.SelectExpression;
import com.googlecode.lazyrecords.sql.expressions.SelectList;
import com.googlecode.lazyrecords.sql.expressions.SetQuantifier;
import com.googlecode.lazyrecords.sql.expressions.ValueExpression;
import com.googlecode.lazyrecords.sql.expressions.WhereClause;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;

public interface SqlGrammar {
    SelectExpression selectExpression(Option<SetQuantifier> setQuantifier,
                                      SelectList selectList,
                                      FromClause fromClause,
                                      Option<WhereClause> whereClause,
                                      Option<OrderByClause> orderByClause);

    SelectList selectList(Sequence<Keyword<?>> select);

    DerivedColumn derivedColumn(Callable1<? super Record, ?> callable);

    FromClause fromClause(Definition definition);

    Option<WhereClause> whereClause(Option<Predicate<? super Record>> where);

    Option<OrderByClause> orderByClause(Option<Comparator<? super Record>> orderBy);


    Expression insertStatement(Definition definition, Record record);

    Expression updateStatement(Definition definition, Predicate<? super Record> predicate, Record record);

    Expression deleteStatement(Definition definition, Option<? extends Predicate<? super Record>> predicate);

    Expression createTable(Definition definition);

    Expression dropTable(Definition definition);

    ValueExpression valueExpression(Callable1<? super Record, ?> callable);

    ValueExpression concat(Sequence<? extends Keyword<?>> keywords);


    class functions {
        public static RecordTo<Expression> insertStatement(final SqlGrammar grammar, final Definition definition) {
            return new RecordTo<Expression>() {
                public Expression call(Record record) throws Exception {
                    return grammar.insertStatement(definition, record);
                }
            };
        }

        public static Function1<Pair<? extends Predicate<? super Record>, Record>, Expression> updateStatement(final SqlGrammar grammar, final Definition definition) {
            return new Function1<Pair<? extends Predicate<? super Record>, Record>, Expression>() {
                public Expression call(Pair<? extends Predicate<? super Record>, Record> recordPair) throws Exception {
                    return grammar.updateStatement(definition, recordPair.first(), recordPair.second());
                }
            };
        }

        public static Function1<Keyword<?>, DerivedColumn> derivedColumn(final SqlGrammar grammar) {
            return new Function1<Keyword<?>, DerivedColumn>() {
                public DerivedColumn call(Keyword<?> keyword) throws Exception {
                    return grammar.derivedColumn(keyword);
                }
            };
        }

    }
}
