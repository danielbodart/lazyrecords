package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.annotations.multimethod;
import com.googlecode.totallylazy.multi;

public class Keywords {
    public static String name(Keyword<?> keyword) {return new multi(){}.<String>methodOption(keyword).getOrElse(keyword.name());}
    @multimethod public static String name(AliasedKeyword<?> keyword) { return name(keyword.source()); }

    public static class functions {
        public static Function1<Keyword<?>, String> name() {
            return new Mapper<Keyword<?>, String>() {
                @Override
                public String call(Keyword<?> keyword) throws Exception {
                    return Keywords.name(keyword);
                }
            };
        }
    }
}
