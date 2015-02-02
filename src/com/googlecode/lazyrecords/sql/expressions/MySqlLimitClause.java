package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.empty;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public class MySqlLimitClause extends AnsiFetchClause {
    public MySqlLimitClause(int number) {
        super(textOnly("limit"), number, empty());
    }
}
