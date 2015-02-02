package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Function1;

public interface OffsetClause extends Expression {
    int number();

    class functions {
        public static Function1<OffsetClause, Integer> number() {
            return new Function1<OffsetClause, Integer>() {
                @Override
                public Integer call(OffsetClause fetchClause) throws Exception {
                    return fetchClause.number();
                }
            };
        }

        public static Function1<Integer, OffsetClause> offsetClause() {
            return new Function1<Integer, OffsetClause>() {
                @Override
                public OffsetClause call(Integer integer) throws Exception {
                    return new AnsiOffsetClause(integer);
                }
            };
        }
    }
}
