package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Files;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class IndexMigratorTest {
    @Test
    @Ignore
    public void shouldMigrateProd() throws Exception {
        File old = new File("../../20121031121454787.bgb.unzipped");
        File newStructure = Files.emptyVMDirectory("partitioned-index");
        IndexMigrator.migrate(old, newStructure);
    }

    @Test
    @Ignore
    public void migrateIndexFile() throws Exception {
        File oldFilesDir = new File("/tmp/jpl/oldIndexFiles");
        File newFilesDir = new File("/tmp/jpl/newIndexFiles");
        newFilesDir.mkdirs();

        for(File child : oldFilesDir.listFiles()){
            IndexMigrator.migrate(child, new File(newFilesDir, child.getName()));
            System.out.println("Migrated " + child.getParent() + "/" + child.getName());
        }
    }
}
