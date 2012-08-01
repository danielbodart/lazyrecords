package com.googlecode.lazyrecords.mappings;

import com.googlecode.totallylazy.Uri;

import java.net.URI;

import static com.googlecode.totallylazy.URLs.uri;

public class UriMappings {
    public static class URIMapping implements StringMapping<URI> {
        public URI toValue(String value) {
            return uri(value);
        }

        public String toString(URI value) {
            return value.toString();
        }
    }

    public static class UriMapping implements StringMapping<Uri> {
        public Uri toValue(String value) {
            return Uri.uri(value);
        }

        public String toString(Uri value) {
            return value.toString();
        }
    }
}
