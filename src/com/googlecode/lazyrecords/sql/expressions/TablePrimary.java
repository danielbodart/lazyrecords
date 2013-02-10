package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;

public interface TablePrimary extends TableReference {
    TableName tableName();
    Option<AsClause> asClause();
}
