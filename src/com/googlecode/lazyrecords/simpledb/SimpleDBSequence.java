package com.googlecode.lazyrecords.simpledb;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SelectCallable;
import com.googlecode.lazyrecords.mappings.LexicalMappings;
import com.googlecode.lazyrecords.sql.expressions.AbstractExpression;
import com.googlecode.lazyrecords.sql.expressions.SelectBuilder;
import com.googlecode.totallylazy.Unchecked;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Iterator;

import static java.lang.String.format;

public class SimpleDBSequence<T> extends Sequence<T> {
    private final AmazonSimpleDB sdb;
    private final SelectBuilder builder;
    private final LexicalMappings mappings;
    private final Callable1<? super Item, T> itemToRecord;
    private final PrintStream logger;
    private final boolean consistentRead;

    public SimpleDBSequence(AmazonSimpleDB sdb, SelectBuilder builder, LexicalMappings mappings, Callable1<? super Item, T> itemToRecord, PrintStream logger, boolean consistentRead) {
        this.sdb = sdb;
        this.builder = builder;
        this.mappings = mappings;
        this.itemToRecord = itemToRecord;
        this.logger = logger;
        this.consistentRead = consistentRead;
    }

    public Iterator<T> iterator() {
        return iterator(builder.express());
    }

    private Iterator<T> iterator(final AbstractExpression expression) {
        String selectExpression = expression.toString(value());
        logger.println("SimpleDB: " + selectExpression);
        return iterator(new SelectRequest(selectExpression, consistentRead)).map(itemToRecord).iterator();
    }

    private Function1<Object, Object> value() {
        return new Function1<Object, Object>() {
            public Object call(Object value) throws Exception {
                return mappings.toString(value.getClass(), value);
            }
        };
    }

    private Sequence<Item> iterator(final SelectRequest selectRequest) {
        SelectResult result = sdb.select(selectRequest);
        Sequence<Item> items = Sequences.sequence(result.getItems());
        String nextToken = result.getNextToken();
        if(nextToken != null){
            return items.join(iterator(selectRequest.withNextToken(nextToken)));
        }
        return items;
    }

    @Override
    public Sequence<T> filter(Predicate<? super T> predicate) {
        return new SimpleDBSequence<T>(sdb, builder.where(Unchecked.<Predicate<Record>>cast(predicate)), mappings, itemToRecord, logger, consistentRead);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> Sequence<S> map(final Callable1<? super T, ? extends S> callable) {
        Callable1 raw = callable;
        if (raw instanceof Keyword) {
            final Keyword<S> keyword = (Keyword<S>) raw;
            return new SimpleDBSequence<S>(sdb, builder.select(keyword), mappings, itemToValue(keyword), logger, consistentRead);
        }
        if (raw instanceof SelectCallable) {
            return (Sequence<S>) new SimpleDBSequence(sdb, builder.select(((SelectCallable) raw).keywords()), mappings, itemToRecord, logger, consistentRead);
        }
        logger.println(format("Warning: Unsupported Callable1 %s dropping down to client side sequence functionality", callable));
        return super.map(callable);
    }

    private <S> Function1<Item, S> itemToValue(final Keyword<S> keyword) {
        return new Function1<Item, S>() {
            public S call(Item item) throws Exception {
                return ((Record) itemToRecord.call(item)).get(keyword);
            }
        };
    }

    @Override
    public Sequence<T> sortBy(Comparator<? super T> comparator) {
        try {
            return new SimpleDBSequence<T>(sdb, builder.orderBy(Unchecked.<Comparator<Record>>cast(comparator)), mappings, itemToRecord, logger, consistentRead);
        } catch (UnsupportedOperationException ex) {
            logger.println(format("Warning: Unsupported Comparator %s dropping down to client side sequence functionality", comparator));
            return super.sortBy(comparator);
        }
    }
}
