package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

public interface TableReference extends Expression {
    TableName tableName();
    Option<AsClause> asClause();
}
