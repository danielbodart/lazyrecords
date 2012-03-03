package com.googlecode.lazyrecords.sql.mappings;

import com.googlecode.lazyrecords.mappings.StringMappings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectMapping implements SqlMapping<Object> {
    private final Class<?> aClass;
    private final StringMappings stringMappings;

    public ObjectMapping(Class<?> aClass, StringMappings stringMappings) {
        this.aClass = aClass;
        this.stringMappings = stringMappings;
    }

    public Object getValue(ResultSet resultSet, Integer index) throws SQLException {
        return stringMappings.toValue(aClass, resultSet.getString(index));
    }

    public void setValue(PreparedStatement statement, Integer index, Object value) throws SQLException {
        statement.setString(index, stringMappings.toString(aClass, value));
    }
}
