package com.googlecode.lazyrecords.sql.mappings;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

public class DateMapping implements SqlMapping<Date> {
    public Date getValue(ResultSet resultSet, Integer index) throws SQLException {
        return dateFrom(resultSet.getTimestamp(index));
    }

    @Override
    public Date getValue(CallableStatement statement, Integer index) throws SQLException {
        return dateFrom(statement.getTimestamp(index));
    }

    public void setValue(PreparedStatement statement, Integer index, Date date) throws SQLException {
        statement.setTimestamp(index, date == null ? null : new Timestamp(date.getTime()));
    }

    @Override
    public int sqlType() {
        return Types.TIMESTAMP;
    }

    public String type() {
        return "timestamp";
    }

    private Date dateFrom(Timestamp timestamp) {
        return timestamp == null ? null : new Date(timestamp.getTime());
    }
}
