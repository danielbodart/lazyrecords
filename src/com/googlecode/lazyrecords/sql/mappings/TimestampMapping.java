package com.googlecode.lazyrecords.sql.mappings;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class TimestampMapping implements SqlMapping<Timestamp> {
    public Timestamp getValue(ResultSet resultSet, Integer index) throws SQLException {
        return resultSet.getTimestamp(index);
    }

    @Override
    public Timestamp getValue(CallableStatement statement, Integer index) throws SQLException {
        return statement.getTimestamp(index);
    }

    public void setValue(PreparedStatement statement, Integer index, Timestamp timestamp) throws SQLException {
        statement.setTimestamp(index, timestamp);
    }

    @Override
    public int type() {
        return Types.TIMESTAMP;
    }
}
