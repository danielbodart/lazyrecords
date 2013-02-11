package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.Joiner;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.On;
import com.googlecode.lazyrecords.Using;
import com.googlecode.lazyrecords.sql.expressions.JoinSpecification;

import java.util.Map;

import static com.googlecode.totallylazy.Unchecked.cast;

public class OracleGrammar extends AnsiSqlGrammar {
    public OracleGrammar(Map<Class, String> mappings) {
        super(mappings);
    }

    public OracleGrammar() {
        this(ColumnDatatypeMappings.oracle());
    }

    @Override
    public JoinSpecification joinSpecification(Joiner joiner) {
        if(joiner instanceof Using) { // TEMP FIX for ORA-25154: column part of USING clause cannot have qualifier
            Keyword<Object> head = cast(((Using) joiner).keywords().head());
            return super.joinSpecification(On.on(head, head));
        }
        return super.joinSpecification(joiner);
    }
}
