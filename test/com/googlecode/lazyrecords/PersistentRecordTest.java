package com.googlecode.lazyrecords;

public class PersistentRecordTest extends RecordContract {
    protected Record createRecord() {
        return Record.constructors.record();
    }
}
