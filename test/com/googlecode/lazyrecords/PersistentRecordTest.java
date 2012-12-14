package com.googlecode.lazyrecords;

import static com.googlecode.lazyrecords.Record.constructors.record;

public class PersistentRecordTest extends RecordContract {
    protected Record createRecord() {
        return Record.constructors.record();
    }
}
