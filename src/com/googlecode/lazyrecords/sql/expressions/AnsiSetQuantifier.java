package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.empty;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public enum AnsiSetQuantifier implements SetQuantifier {
    DISTINCT {
        @Override
        public String text() {
            return "distinct";
        }
    },
    ALL{
        @Override
        public String text() {
            return "";
        }
    };

    @Override
    public Sequence<Object> parameters() {
        return Sequences.empty();
    }
}
