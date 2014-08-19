package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Functor;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.multi;

import static com.googlecode.totallylazy.Unchecked.cast;

public abstract class AbstractQualifier {
    private multi multi;
    public <T extends Expression> T qualify(final T expression) {
        if(multi == null) multi = new multi(){};
        return multi.<T>methodOption(expression).getOrElse(expression);
    }

    public  <T extends Expression, M extends Functor<T>> M qualify(M items) {
        return cast(items.map(this.<T>qualify()));
    }

    private <T extends Expression> UnaryFunction<T> qualify() {
        return new UnaryFunction<T>() {
            @Override
            public T call(T expression) throws Exception {
                return qualify(expression);
            }
        };
    }
}
