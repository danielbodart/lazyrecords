package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Function1;

public interface FetchClause extends Expression {
    int number();

    class functions {
        public static Function1<FetchClause, Integer> number() {
            return new Function1<FetchClause, Integer>() {
                @Override
                public Integer call(FetchClause fetchClause) throws Exception {
                    return fetchClause.number();
                }
            };
        }

        public static Function1<Integer, FetchClause> fetchClause() {
            return new Function1<Integer, FetchClause>() {
                @Override
                public FetchClause call(Integer integer) throws Exception {
                    return new AnsiFetchClause(integer);
                }
            };
        }
    }
}
