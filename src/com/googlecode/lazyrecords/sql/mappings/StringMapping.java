package com.googlecode.lazyrecords.sql.mappings;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class StringMapping implements SqlMapping<String> {
    @Override
    public String getValue(ResultSet resultSet, Integer index) throws SQLException {
        return resultSet.getString(index);
    }

    @Override
    public String getValue(CallableStatement statement, Integer index) throws SQLException {
        return statement.getString(index);
    }

    @Override
    public void setValue(PreparedStatement statement, Integer index, String instance) throws SQLException {
        statement.setString(index, instance);
    }

    @Override
    public int sqlType() {
        return Types.VARCHAR;
    }
}
