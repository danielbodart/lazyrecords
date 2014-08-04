package com.googlecode.lazyrecords.sql.grammars;

import com.googlecode.lazyrecords.Aggregate;
import com.googlecode.lazyrecords.JoinStringWithSeparator;
import com.googlecode.lazyrecords.Joiner;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.On;
import com.googlecode.lazyrecords.Using;
import com.googlecode.lazyrecords.sql.expressions.CompoundExpression;
import com.googlecode.lazyrecords.sql.expressions.Expressions;
import com.googlecode.lazyrecords.sql.expressions.JoinSpecification;
import com.googlecode.lazyrecords.sql.expressions.ValueExpression;
import com.googlecode.totallylazy.annotations.multimethod;

import java.util.Map;

import static com.googlecode.lazyrecords.sql.expressions.SetFunctionType.setFunctionType;
import static com.googlecode.lazyrecords.sql.expressions.TextOnlyExpression.textOnly;
import static com.googlecode.totallylazy.Sequences.sequence;
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

    @Override @multimethod
    public ValueExpression valueExpression(Aggregate aggregate) {
        if (aggregate.reducer() instanceof JoinStringWithSeparator) {
            return new OracleGroupConcatExpression(aggregate.source(), ((JoinStringWithSeparator) aggregate.reducer()).getSeparator());
        }
        return setFunctionType(aggregate.reducer(), aggregate.source());
    }

    private static class OracleGroupConcatExpression extends CompoundExpression implements ValueExpression {
        private OracleGroupConcatExpression(Keyword aggregate, String separator) {
            super(sequence(textOnly("listagg("), Expressions.columnReference(aggregate), textOnly(",'"), textOnly(separator), textOnly("') within group(order by "), Expressions.columnReference(aggregate), textOnly(")")), "");
        }
    }
}
