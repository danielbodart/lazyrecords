package com.googlecode.lazyrecords;

import static com.googlecode.lazyrecords.Record.constructors.immutableRecord;

public class ImmutableMapRecordTest extends RecordContract {
    protected Record createRecord() {
        return immutableRecord();
    }
}
