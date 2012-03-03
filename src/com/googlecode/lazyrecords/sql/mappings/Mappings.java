package com.googlecode.lazyrecords.sql.mappings;

import com.googlecode.lazyrecords.mappings.LexicalMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.numbers.Numbers.numbers;
import static com.googlecode.totallylazy.numbers.Numbers.range;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class Mappings {
    private final Map<Class, SqlMapping<Object>> map = new HashMap<Class, SqlMapping<Object>>();
    private final LexicalMappings lexicalMappings;

    public Mappings(LexicalMappings lexicalMappings) {
        this.lexicalMappings = lexicalMappings;
        add(Boolean.class, new BooleanMapping());
        add(Date.class, new DateMapping());
        add(Timestamp.class, new TimestampMapping());
        add(Integer.class, new IntegerMapping());
        add(Long.class, new LongMapping());
    }

    public Mappings() {
        this(new LexicalMappings());
    }

    public <T> Mappings add(final Class<T> type, final SqlMapping<T> mapping) {
        map.put(type, Unchecked.<SqlMapping<Object>>cast(mapping));
        return this;
    }

    public SqlMapping<Object> get(final Class aClass) {
        if (!map.containsKey(aClass)) {
            return new ObjectMapping(aClass, lexicalMappings);
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
