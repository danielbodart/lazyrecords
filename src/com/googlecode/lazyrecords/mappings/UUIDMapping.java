package com.googlecode.lazyrecords.mappings;

import java.util.UUID;

public class UUIDMapping implements LexicalMapping<UUID> {
    public UUID toValue(String value) {
        return UUID.fromString(value);
    }

    public String toString(UUID value) {
        return value.toString();
    }
}
