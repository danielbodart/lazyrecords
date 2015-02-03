package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.sql.expressions.FetchClause;
import com.googlecode.lazyrecords.sql.expressions.OffsetClause;

import java.util.Map;

public class Oracle11Grammar extends OracleGrammar {
    public Oracle11Grammar(Map<Class, String> mappings) {
        super(mappings);
    }

    public Oracle11Grammar() {
    }

    @Override
    public OffsetClause offsetClause(int number) {
        throw new UnsupportedOperationException("Oracle 11 does not support OFFSET");
    }

    @Override
    public FetchClause fetchClause(int number) {
        throw new UnsupportedOperationException("Oracle 11 does not support FETCH");
    }
}
