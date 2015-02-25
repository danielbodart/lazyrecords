package com.googlecode.lazyrecords.sql.mappings;

import java.sql.*;

public class LongMapping implements SqlMapping<Long> {
    public Long getValue(ResultSet resultSet, Integer index) throws SQLException {
        long result = resultSet.getLong(index);
        return resultSet.wasNull() ? null : result;
    }

    @Override
    public Long getValue(CallableStatement statement, Integer index) throws SQLException {
        long result = statement.getLong(index);
        return statement.wasNull() ? null : result;
    }

    public void setValue(PreparedStatement statement, Integer index, Long value) throws SQLException {
        if(value == null) {
            statement.setNull(index, type());
        } else {
            statement.setLong(index, value);
        }
    }

    @Override
    public int type() {
        return Types.BIGINT;
    }
}
