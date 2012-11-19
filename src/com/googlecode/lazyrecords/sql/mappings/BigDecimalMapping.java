package com.googlecode.lazyrecords.sql.mappings;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalMapping implements SqlMapping<BigDecimal> {
    public BigDecimal getValue(ResultSet resultSet, Integer index) throws SQLException {
		BigDecimal result = resultSet.getBigDecimal(index);
        if (resultSet.wasNull()) {
            return null;
        }
        return result;
    }

    public void setValue(PreparedStatement statement, Integer index, BigDecimal value) throws SQLException {
        statement.setBigDecimal(index, value);
    }
}
