package com.googlecode.lazyrecords.sql.mappings;

import com.googlecode.lazyrecords.mappings.StringMapping;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.numbers;
import static com.googlecode.totallylazy.numbers.Numbers.range;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class SqlMappings {
    private final Map<Class, SqlMapping<Object>> map = new HashMap<Class, SqlMapping<Object>>();
    private final StringMappings stringMappings;

    public SqlMappings(StringMappings stringMappings) {
        this.stringMappings = stringMappings;
        add(Boolean.class, new BooleanMapping());
        add(Date.class, new DateMapping());
        add(Timestamp.class, new TimestampMapping());
        add(Integer.class, new IntegerMapping());
        add(Long.class, new LongMapping());
    }

    public SqlMappings() {
        this(new StringMappings());
    }

    public <T> SqlMappings add(final Class<T> type, final SqlMapping<T> mapping) {
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
                return numbers(statement.executeBatch()).reduce(sum());
            }
        };
    }


}