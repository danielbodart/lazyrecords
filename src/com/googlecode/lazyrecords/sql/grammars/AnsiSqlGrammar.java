package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.Aliased;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.sql.expressions.*;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;

import java.util.Comparator;
import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.name;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;

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
                                             Sequence<Keyword<?>> selectList,
                                             Definition fromClause,
                                             Option<Predicate<? super Record>> whereClause,
                                             Option<Comparator<? super Record>> orderByClause) {
        return AnsiSelectExpression.selectExpression(setQuantifier, selectList(selectList), fromClause(fromClause),
                whereClause.map(functions.whereClause(this)), orderByClause.map(functions.orderByClause(this)));
    }

    @Override
    public SelectList selectList(Sequence<Keyword<?>> select) {
        return AnsiSelectList.selectList(select.map(functions.derivedColumn(this)));
    }

    @Override
    public FromClause fromClause(Definition definition) {
        return AnsiFromClause.fromClause(definition);
    }

    @Override
    public WhereClause whereClause(Predicate<? super Record> where) {
        return AnsiWhereClause.whereClause(where);
    }

    @Override
    public OrderByClause orderByClause(Comparator<? super Record> orderBy) {
        return AnsiOrderByClause.orderByClause(orderBy);
    }

    @Override
    public DerivedColumn derivedColumn(Callable1<? super Record, ?> callable) {
        ValueExpression expression = valueExpression(callable);
        if (callable instanceof Aliased) {
            return AnsiDerivedColumn.derivedColumn(expression, some(AnsiAsClause.asClause(((Keyword<?>) callable).name())));
        }
        return AnsiDerivedColumn.derivedColumn(expression, none(AsClause.class));
    }

    @Override
    public ValueExpression valueExpression(Callable1<? super Record, ?> callable) {
        return AnsiValueExpression.valueExpression(callable);
    }

    @Override
    public ValueExpression concat(Sequence<? extends Keyword<?>> keywords) {
        return AnsiValueExpression.concat(keywords);
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
