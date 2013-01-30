package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.*;
import com.googlecode.totallylazy.*;

import java.util.Comparator;

public interface SqlGrammar {
    SelectExpression selectExpression(Option<SetQuantifier> setQuantifier,
                                      SelectList selectList,
                                      FromClause fromClause,
                                      Option<WhereClause> whereClause,
                                      Option<OrderByClause> orderByClause);

    SelectList selectList(Sequence<Keyword<?>> select);

    FromClause fromClause(Definition definition);

    Option<WhereClause> whereClause(Option<Predicate<? super Record>> where);

    Option<OrderByClause> orderByClause(Option<Comparator<? super Record>> orderBy);


    Expression insertStatement(Definition definition, Record record);

    Expression updateStatement(Definition definition, Predicate<? super Record> predicate, Record record);

    Expression deleteStatement(Definition definition, Option<? extends Predicate<? super Record>> predicate);

    Expression createTable(Definition definition);

    Expression dropTable(Definition definition);




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
    }
}
