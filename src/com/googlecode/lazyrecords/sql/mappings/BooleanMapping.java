package com.googlecode.lazyrecords.sql.mappings;

import java.sql.*;

public class BooleanMapping implements SqlMapping<Boolean> {
    public Boolean getValue(ResultSet resultSet, Integer index) throws SQLException {
        boolean result = resultSet.getBoolean(index);
        return resultSet.wasNull() ? null : result;
    }

    @Override
    public Boolean getValue(CallableStatement statement, Integer index) throws SQLException {
        boolean result = statement.getBoolean(index);
        return statement.wasNull() ? null : result;
    }

    public void setValue(PreparedStatement statement, Integer index, Boolean value) throws SQLException {
        if(value == null) {
            statement.setNull(index, type());
        } else {
            statement.setBoolean(index, value);
        }
    }

    @Override
    public int type() {
        return Types.BOOLEAN;
    }
}
