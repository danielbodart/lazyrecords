package com.googlecode.lazyrecords.sql.mappings;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class BooleanMapping implements SqlMapping<Boolean> {
    public Boolean getValue(ResultSet resultSet, Integer index) throws SQLException {
        return resultSet.getBoolean(index);
    }

    @Override
    public Boolean getValue(CallableStatement statement, Integer index) throws SQLException {
        return statement.getBoolean(index);
    }

    public void setValue(PreparedStatement statement, Integer index, Boolean value) throws SQLException {
        statement.setBoolean(index, value);
    }

    @Override
    public int sqlType() {
        return Types.BOOLEAN;
    }
}
