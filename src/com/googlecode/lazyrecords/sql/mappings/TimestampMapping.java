package com.googlecode.lazyrecords.sql.mappings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampMapping implements SqlMapping<Timestamp> {
    public Timestamp getValue(ResultSet resultSet, Integer index) throws SQLException {
        return resultSet.getTimestamp(index);
    }

    public void setValue(PreparedStatement statement, Integer index, Timestamp timestamp) throws SQLException {
        statement.setTimestamp(index, timestamp);
    }
}
