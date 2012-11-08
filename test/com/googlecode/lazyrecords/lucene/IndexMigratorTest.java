package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Files;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class IndexMigratorTest {
    @Test
    @Ignore
    public void shouldMigrateProd() throws Exception {
        File old = new File("../../20121031121454787.bgb.unzipped");
        File newStructure = Files.emptyTemporaryDirectory("partitioned-index");
        IndexMigrator.migrate(old, newStructure);

    }
}
