package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.RecordsDefiner;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.lazyrecords.sql.expressions.TableDefinition.dropTable;
import static com.googlecode.lazyrecords.sql.expressions.TableDefinition.tableDefinition;
import static com.googlecode.totallylazy.Predicates.is;

public class SqlRecordsDefiner implements RecordsDefiner {
    private final SqlRecords sqlRecords;
    private final CreateTable createTable;

    public SqlRecordsDefiner(SqlRecords sqlRecords, CreateTable createTable) {
        this.sqlRecords = sqlRecords;
        this.createTable = createTable;
    }

    @Override
    public void define(Definition definition) {
        if(createTable.equals(CreateTable.Disabled)){
            return;
        }
        if (exists(definition)) {
            return;
        }
        sqlRecords.update(tableDefinition(definition));
    }

    private static final Keyword<Integer> one = keyword("1", Integer.class);

    @Override
    public boolean exists(Definition definition) {
        try {
            sqlRecords.query(from(definition).select(one).where(Predicates.where(one, is(2))).build(), Sequences.<Keyword<?>>empty()).realise();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void undefine(Definition definition) {
        if(exists(definition)){
            sqlRecords.update(dropTable(definition));
        }
    }
}
