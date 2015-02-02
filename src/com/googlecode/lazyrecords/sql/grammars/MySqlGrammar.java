package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.sql.expressions.*;

import java.util.Map;

public class MySqlGrammar extends AnsiSqlGrammar {
    public MySqlGrammar(Map<Class, String> mappings) {
        super(mappings);
    }

    public MySqlGrammar() {
        this(ColumnDatatypeMappings.mysql());
    }

    @Override
    public OffsetClause offsetClause(int number) {
        return new MySqlOffsetClause(number);
    }

    @Override
    public FetchClause fetchClause(int number) {
        return new MySqlLimitClause(number);
    }
}
