package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.sql.expressions.FetchClause;
import com.googlecode.lazyrecords.sql.expressions.LimitClause;

import java.util.Map;

public class MySqlGrammar extends AnsiSqlGrammar {
    public MySqlGrammar(Map<Class, String> mappings) {
        super(mappings);
    }

    public MySqlGrammar() {
        this(ColumnDatatypeMappings.mysql());
    }

    @Override
    public FetchClause fetchClause(int number) {
        return new LimitClause(number);
    }
}
