package com.googlecode.lazyrecords.sql.expressions;

import static com.googlecode.lazyrecords.sql.expressions.Expressions.empty;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.textOnly;

public enum SetQuantifier {
    DISTINCT,
    ALL;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static Expression setQuantifier(SetQuantifier setQuantifier) {
        if(setQuantifier.equals(SetQuantifier.ALL)){
            return empty();
        }
        return textOnly(setQuantifier);
    }
}
