package com.googlecode.lazyrecords.sql.mappings;

import org.hsqldb.types.Types;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalMapping implements SqlMapping<BigDecimal> {
    @Override
    public BigDecimal getValue(ResultSet resultSet, Integer index) throws SQLException {
        BigDecimal result = resultSet.getBigDecimal(index);
        return resultSet.wasNull() ? null : result;
    }

    @Override
    public BigDecimal getValue(CallableStatement statement, Integer index) throws SQLException {
        BigDecimal result = statement.getBigDecimal(index);
        return statement.wasNull() ? null : result;
    }

    @Override
    public void setValue(PreparedStatement statement, Integer index, BigDecimal value) throws SQLException {
        statement.setBigDecimal(index, value);
    }

    @Override
    public int type() {
        return Types.NUMERIC;
    }
}
