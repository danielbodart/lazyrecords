package com.googlecode.lazyrecords.mappings;

import com.googlecode.totallylazy.Uri;

public class UriMapping implements StringMapping<Uri> {
    public Uri toValue(String value) {
        return Uri.uri(value);
    }

    public String toString(Uri value) {
        return value.toString();
    }
}