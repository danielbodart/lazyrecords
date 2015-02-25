package com.googlecode.lazyrecords.sql.mappings;

import java.sql.*;

public class BooleanMapping implements SqlMapping<Boolean> {
    public Boolean getValue(ResultSet resultSet, Integer index) throws SQLException {
        return resultSet.getBoolean(index);
    }

    @Override
    public Boolean getValue(CallableStatement statement, Integer index) throws SQLException {
        return statement.getBoolean(index);
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
