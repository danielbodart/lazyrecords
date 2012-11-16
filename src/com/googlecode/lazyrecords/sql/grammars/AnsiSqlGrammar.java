package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.DeleteStatement;
import com.googlecode.lazyrecords.sql.expressions.Expression;
import com.googlecode.lazyrecords.sql.expressions.InsertStatement;
import com.googlecode.lazyrecords.sql.expressions.SelectExpression;
import com.googlecode.lazyrecords.sql.expressions.SetQuantifier;
import com.googlecode.lazyrecords.sql.expressions.TableDefinition;
import com.googlecode.lazyrecords.sql.expressions.UpdateStatement;
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
    public Expression selectExpression(Definition definition, Sequence<Keyword<?>> select, SetQuantifier setQuantifier, Option<Predicate<? super Record>> where, Option<Comparator<? super Record>> orderBy, Option<Join> join) {
        return SelectExpression.selectExpression(definition, select, setQuantifier, where, orderBy, join);
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
