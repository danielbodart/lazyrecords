package com.googlecode.lazyrecords;

import org.junit.Test;

import static com.googlecode.lazyrecords.Record.constructors.record;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MapRecordTest extends RecordContract {
    protected Record createRecord() {
        return record();
    }
}
