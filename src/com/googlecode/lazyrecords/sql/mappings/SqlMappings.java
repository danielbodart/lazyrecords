package com.googlecode.lazyrecords.sql.mappings;

import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.Map;

import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.*;

public class SqlMappings {
    private final Map<Class, SqlMapping<Object>> map = map();
    private final StringMappings stringMappings;

    public SqlMappings(StringMappings stringMappings) {
        this.stringMappings = stringMappings;
        add(Boolean.class, new BooleanMapping());
        add(Date.class, new DateMapping());
        add(Timestamp.class, new TimestampMapping());
        add(Integer.class, new IntegerMapping());
        add(BigDecimal.class, new BigDecimalMapping());
        add(Number.class, new BigDecimalMapping());
        add(Long.class, new LongMapping());
    }

    public SqlMappings() {
        this(new StringMappings());
    }

    public <T> SqlMappings add(final Class<T> type, final SqlMapping<? extends T> mapping) {
        map.put(type, Unchecked.<SqlMapping<Object>>cast(mapping));
        return this;
    }

    public <T> SqlMappings add(final Class<T> type, final StringMapping<T> mapping) {
        stringMappings.add(type, mapping);
        return this;
    }

    public SqlMapping<Object> get(final Class aClass) {
        if (!map.containsKey(aClass)) {
            return new ObjectMapping(aClass, stringMappings);
        }
        return map.get(aClass);
    }

    public Object getValue(final ResultSet resultSet, Integer index, final Class aClass) throws SQLException {
        return get(aClass).getValue(resultSet, index);
    }

    public void addValues(PreparedStatement statement, Sequence<Object> values) throws SQLException {
        for (Pair<Integer, Object> pair : range(1).safeCast(Integer.class).zip(values)) {
            Integer index = pair.first();
            Object value = pair.second();
            get(value == null ? Object.class : value.getClass()).setValue(statement, index, value);
        }
    }

    public Function1<PreparedStatement, Number> addValuesInBatch(final Sequence<? extends Iterable<Object>> allValues) {
        return new Function1<PreparedStatement, Number>() {
            public Number call(PreparedStatement statement) throws Exception {
                for (Iterable<Object> values : allValues) {
                    addValues(statement, sequence(values));
                    statement.addBatch();
                }
                return numbers(statement.executeBatch()).filter(not(Statement.SUCCESS_NO_INFO)).fold(0, sum());
            }
        };
    }


}
