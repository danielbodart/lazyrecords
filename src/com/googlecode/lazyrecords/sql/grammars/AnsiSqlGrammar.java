package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.*;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;
import java.util.Map;

public class AnsiSqlGrammar implements SqlGrammar {
    private final Map<Class, String> mappings;

    public AnsiSqlGrammar(Map<Class, String> mappings) {
        this.mappings = mappings;
    }

    public AnsiSqlGrammar() {
        this(ColumnDatatypeMappings.defaultMappings());
    }

    @Override
    public SelectExpression selectExpression(Option<SetQuantifier> setQuantifier,
                                             SelectList selectList,
                                             FromClause fromClause,
                                             Option<WhereClause> whereClause,
                                             Option<OrderByClause> orderByClause) {
        return AnsiSelectExpression.selectExpression(setQuantifier, selectList, fromClause, whereClause, orderByClause);
    }

    @Override
    public SelectList selectList(Sequence<Keyword<?>> select) {
        return AnsiSelectList.selectList(this, select);
    }

    @Override
    public FromClause fromClause(Definition definition) {
        return AnsiFromClause.fromClause(definition);
    }

    @Override
    public Option<WhereClause> whereClause(Option<Predicate<? super Record>> where) {
        return AnsiWhereClause.whereClause(where);
    }

    @Override
    public Option<OrderByClause> orderByClause(Option<Comparator<? super Record>> orderBy) {
        return AnsiOrderByClause.orderByClause(orderBy);
    }



    @Override
    public Expression insertStatement(Definition definition, Record record) {
        return InsertStatement.insertStatement(definition, record);
    }

    @Override
    public Expression updateStatement(Definition definition, Predicate<? super Record> predicate, Record record) {
        return UpdateStatement.updateStatement(definition, predicate, record);
    }

    @Override
    public Expression deleteStatement(Definition definition, Option<? extends Predicate<? super Record>> predicate) {
        return DeleteStatement.deleteStatement(definition, predicate);
    }

    @Override
    public Expression createTable(Definition definition) {
        return TableDefinition.createTable(definition, mappings);
    }

    @Override
    public Expression dropTable(Definition definition) {
        return TableDefinition.dropTable(definition);
    }

}
