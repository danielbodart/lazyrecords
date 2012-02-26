package com.googlecode.lazyrecords.simpledb;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.Item;
import com.googlecode.lazyrecords.AbstractRecords;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Schema;
import com.googlecode.lazyrecords.simpledb.mappings.Mappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.Value;
import com.googlecode.totallylazy.numbers.Numbers;

import java.io.PrintStream;
import java.util.List;

import static com.googlecode.lazyrecords.SelectCallable.select;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.totallylazy.Streams.nullPrintStream;
import static com.googlecode.totallylazy.numbers.Numbers.sum;

public class SimpleDBRecords extends AbstractRecords {
    private final AmazonSimpleDB sdb;
    private final Mappings mappings;
    private final PrintStream logger;
    private final boolean consistentRead;
    private final Schema schema;

    public SimpleDBRecords(final AmazonSimpleDB sdb, boolean consistentRead, final Mappings mappings, final PrintStream logger, final Schema schema) {
        this.consistentRead = consistentRead;
        this.mappings = mappings;
        this.sdb = sdb;
        this.logger = logger;
        this.schema = schema;
    }

    public SimpleDBRecords(final AmazonSimpleDB sdb, boolean consistentRead) {
        this(sdb, consistentRead, new Mappings(), nullPrintStream(), new SimpleDBSchema(sdb));
    }

    public Sequence<Record> get(Definition definition) {
        return new SimpleDBSequence<Record>(sdb, from(definition), mappings, mappings.asRecord(definition.fields()), logger, consistentRead);
    }

    public Number add(final Definition definition, Sequence<Record> records) {
        if (records.isEmpty()) {
            return 0;
        }

        return records.recursive(Sequences.<Record>splitAt(25)).
                mapConcurrently(putAttributes(definition)).
                reduce(sum());
    }

    public Number remove(Definition definition, Predicate<? super Record> predicate) {
        if (!schema.exists(definition)) {
            return 0;
        }
        Sequence<Record> items = get(definition).filter(predicate).realise();
        if (items.isEmpty()) {
            return 0;
        }

        return items.recursive(Sequences.<Record>splitAt(25)).
                mapConcurrently(deleteAttributes(definition)).
                reduce(sum());
    }

    @Override
    public Number remove(Definition definition) {
        Record head = get(definition).map(select(Keywords.keyword("count(*)", String.class))).head();
        Number result = Numbers.valueOf(head.get(Keywords.keyword("Count", String.class))).get();
        schema.undefine(definition);
        schema.define(definition);
        return result;
    }

    private Function1<Value<Item>, DeletableItem> asItem() {
        return new Function1<Value<Item>, DeletableItem>() {
            public DeletableItem call(Value<Item> value) throws Exception {
                return new DeletableItem().withName(value.value().getName());
            }
        };
    }

    private Function1<Sequence<Record>, Number> putAttributes(final Definition definition) {
        return new Function1<Sequence<Record>, Number>() {
            public Number call(Sequence<Record> batch) throws Exception {
                sdb.batchPutAttributes(new BatchPutAttributesRequest(definition.name(), batch.map(mappings.toReplaceableItem()).toList()));
                return batch.size();
            }
        };
    }

    private Function1<Sequence<Record>, Number> deleteAttributes(final Definition definition) {
        return new Function1<Sequence<Record>, Number>() {
            public Number call(Sequence<Record> batch) throws Exception {
                List<DeletableItem> items = batch.<Value<Item>>unsafeCast().map(asItem()).toList();
                sdb.batchDeleteAttributes(new BatchDeleteAttributesRequest(definition.name(), items));
                return batch.size();
            }
        };
    }
}
