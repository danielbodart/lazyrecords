package com.googlecode.lazyrecords.sql.mappings;

import java.sql.*;

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
        if(value == null) {
            statement.setNull(index, type());
        } else {
            statement.setInt(index, value);
        }
    }

    @Override
    public int type() {
        return Types.INTEGER;
    }
}
