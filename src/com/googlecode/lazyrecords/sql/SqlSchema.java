package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Schema;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.Sequences;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.sql.expressions.AnsiSelectBuilder.from;
import static com.googlecode.totallylazy.Predicates.alwaysFalse;

public class SqlSchema implements Schema {
    private final SqlRecords records;
    private final SqlGrammar grammar;

    public SqlSchema(SqlRecords records, SqlGrammar grammar) {
        this.records = records;
        this.grammar = grammar;
    }

    @Override
    public void define(Definition definition) {
        if (exists(definition)) {
            return;
        }
        records.update(grammar.createTable(definition));
    }

    public static final Keyword<Integer> one = keyword("1", Integer.class);

    @Override
    public boolean exists(Definition definition) {
        try {
            if (metadataExists(definition)) return true;
            records.query(from(grammar, definition.metadata(Keywords.alias, null)).select(one).filter(alwaysFalse()).build(), Sequences.<Keyword<?>>empty()).realise();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean metadataExists(final Definition definition) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = records.connection().getMetaData().getTables(null, null, definition.name().toUpperCase(), new String[]{"TABLE"});
            return resultSet.next();
        } finally {
            if (resultSet != null) resultSet.close();
        }
    }

    @Override
    public void undefine(Definition definition) {
        if (exists(definition)) {
            records.update(grammar.dropTable(definition));
        }
    }
}
