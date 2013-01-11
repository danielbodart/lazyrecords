package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Schema;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.totallylazy.Predicates.alwaysFalse;
import static com.googlecode.totallylazy.Predicates.is;

public class SqlSchema implements Schema {
    private final SqlRecords sqlRecords;
    private final SqlGrammar grammar;

    public SqlSchema(SqlRecords sqlRecords, SqlGrammar grammar) {
        this.sqlRecords = sqlRecords;
        this.grammar = grammar;
    }

    @Override
    public void define(Definition definition) {
        if (exists(definition)) {
            return;
        }
        sqlRecords.update(grammar.createTable(definition));
    }

    public static final Keyword<Integer> one = keyword("1", Integer.class);

    @Override
    public boolean exists(Definition definition) {
        try {
            sqlRecords.query(from(grammar, definition).select(one).where(alwaysFalse()).build(), Sequences.<Keyword<?>>empty()).realise();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void undefine(Definition definition) {
        if(exists(definition)){
            sqlRecords.update(grammar.dropTable(definition));
        }
    }
}
