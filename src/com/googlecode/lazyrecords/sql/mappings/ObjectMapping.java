package com.googlecode.lazyrecords.sql.mappings;

import com.googlecode.lazyrecords.mappings.LexicalMappings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectMapping implements SqlMapping<Object> {
    private final Class<?> aClass;
    private final LexicalMappings lexicalMappings;

    public ObjectMapping(Class<?> aClass, LexicalMappings lexicalMappings) {
        this.aClass = aClass;
        this.lexicalMappings = lexicalMappings;
    }

    public Object getValue(ResultSet resultSet, Integer index) throws SQLException {
        return lexicalMappings.toValue(aClass, resultSet.getString(index));
    }

    public void setValue(PreparedStatement statement, Integer index, Object value) throws SQLException {
        statement.setString(index, lexicalMappings.toString(aClass, value));
    }
}
