package com.googlecode.lazyrecords;

import org.junit.Before;

import static com.googlecode.lazyrecords.RecordsContract.Books.books;
import static com.googlecode.lazyrecords.RecordsContract.People.people;

public abstract class SchemaBasedRecordContract<T extends Records> extends RecordsContract<T> {

    protected Schema schema;

    @Before
    @Override
    public void setupData() {
        schema.undefine(people);
        schema.define(people);
        schema.undefine(books);
        schema.define(books);
        super.setupData();
    }

}
