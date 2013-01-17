package com.googlecode.lazyrecords;

public interface Named {
    String name();

    class constructors{
        public static Named named(final String name){
            return new Named() {
                @Override
                public String name() {
                    return name;
                }
            };
        }
    }
}
