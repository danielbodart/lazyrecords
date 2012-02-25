package com.googlecode.lazyrecords.simpledb;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Schema;

public class SimpleDBSchema implements Schema {
    private final AmazonSimpleDB sdb;

    public SimpleDBSchema(AmazonSimpleDB sdb) {
        this.sdb = sdb;
    }

    @Override
    public void define(Definition definition) {
        sdb.createDomain(new CreateDomainRequest(definition.name()));
    }

    @Override
    public boolean exists(Definition definition) {
        return sdb.listDomains().withDomainNames(definition.name()).getDomainNames().size() > 0;
    }

    @Override
    public void undefine(Definition definition) {
        sdb.deleteDomain(new DeleteDomainRequest(definition.name()));
    }
}
