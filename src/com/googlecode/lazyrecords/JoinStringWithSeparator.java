package com.googlecode.lazyrecords;

import com.googlecode.totallylazy.Reducer;

public class JoinStringWithSeparator<T> implements Reducer<T, String> {

    private final String separator;

    public JoinStringWithSeparator(String separator) {
        this.separator = separator;
    }

    @Override
    public String call(String acc, T value) throws Exception {
        final String prefix = acc.equals(identity()) ? "" : acc + separator;
        return prefix + value.toString();
    }

    @Override
    public String identity() {
        return "";
    }

    public String getSeparator() {
        return separator;
    }
}
