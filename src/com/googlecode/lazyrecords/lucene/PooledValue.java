package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function1;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.googlecode.totallylazy.Closeables.safeClose;

public class PooledValue implements Closeable {
    private final Searcher searcher;
    private final LuceneSearcher luceneSearcher;
    private volatile boolean dirty = false;
    private final AtomicInteger checkoutCount;

    PooledValue(Searcher searcher, LuceneSearcher luceneSearcher) {
        this.searcher = searcher;
        this.luceneSearcher = luceneSearcher;
        checkoutCount = new AtomicInteger(1);
    }

    public static Function1<PooledValue, Boolean> isDirty() {
        return new Function1<PooledValue, Boolean>() {
            @Override
            public Boolean call(PooledValue value) {
                return value.dirty();
            }
        };
    }

    public boolean dirty() {
        return dirty;
    }

    public void dirty(boolean value) {
        this.dirty = value;
    }

    public static Function1<PooledValue, Searcher> checkoutValue() {
        return new Function1<PooledValue, Searcher>() {
            @Override
            public Searcher call(PooledValue pooledValue) throws Exception {
                return pooledValue.checkout();
            }
        };
    }

    public static Function1<PooledValue, Searcher> searcher() {
        return new Function1<PooledValue, Searcher>() {
            @Override
            public Searcher call(PooledValue pooledValue) throws Exception {
                return pooledValue.searcher;
            }
        };
    }

    public int checkoutCount() {
        return checkoutCount.get();
    }

    private Searcher checkout() {
        checkoutCount.incrementAndGet();
        return searcher;
    }

    public static Function1<PooledValue, Integer> theCheckoutCount() {
        return new Function1<PooledValue, Integer>() {
            @Override
            public Integer call(PooledValue pooledValue) throws Exception {
                return pooledValue.checkoutCount();
            }
        };
    }

    public int checkin() {
        return checkoutCount.decrementAndGet();
    }

    public static Block<PooledValue> markAsDirty() {
        return new Block<PooledValue>() {
            @Override
            public void execute(PooledValue pooledValue) throws Exception {
                pooledValue.dirty(true);
            }
        };
    }

    @Override
    public void close() throws IOException {
        safeClose(luceneSearcher);
    }
}
