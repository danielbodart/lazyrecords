package com.googlecode.lazyrecords.sql.mappings;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class IntegerMapping implements SqlMapping<Integer> {
    public Integer getValue(ResultSet resultSet, Integer index) throws SQLException {
        int result = resultSet.getInt(index);
        return resultSet.wasNull() ? null : result;
    }

    @Override
    public Integer getValue(CallableStatement statement, Integer index) throws SQLException {
        final int result = statement.getInt(index);
        return statement.wasNull() ? null : result;
    }

    public void setValue(PreparedStatement statement, Integer index, Integer value) throws SQLException {
        statement.setInt(index, value);
    }

    @Override
    public int type() {
        return Types.INTEGER;
    }
}
