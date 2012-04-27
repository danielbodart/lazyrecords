package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Function;
import com.googlecode.totallylazy.Function1;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Runnables.VOID;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.lazyrecords.lucene.PooledValue.theCheckoutCount;
import static com.googlecode.lazyrecords.lucene.PooledValue.checkoutValue;
import static com.googlecode.lazyrecords.lucene.PooledValue.isDirty;

public class OptimisedPool implements SearcherPool {
    private final List<PooledValue> pool = new CopyOnWriteArrayList<PooledValue>();
    private final Directory directory;

    public OptimisedPool(Directory directory) {
        this.directory = directory;
    }

    public synchronized int size() {
        return pool.size();
    }

    public PooledValue get(int index) {
        return pool.get(index);
    }

    @Override
    public synchronized Searcher searcher() throws IOException {
        return sequence(pool).find(where(isDirty(), is(false))).
                map(checkoutValue()).
                getOrElse(createSearcher());
    }

    private Function<Searcher> createSearcher() throws IOException {
        return new Function<Searcher>() {
            @Override
            public Searcher call() throws Exception {
                return create();
            }
        };
    }

    private synchronized Searcher create() throws IOException {
        LuceneSearcher luceneSearcher = createLuceneSearcher();
        PooledSearcher searcher = new PooledSearcher(new RecoveringSearcher(luceneSearcher, createNewLuceneSearcher()), checkIn());
        pool.add(new PooledValue(searcher, luceneSearcher));
        return searcher;
    }

    private LuceneSearcher createLuceneSearcher() throws IOException {
        return new LuceneSearcher(new IndexSearcher(directory));
    }

    private Function1<Searcher, Void> checkIn() {
        return new Function1<Searcher, Void>() {
            @Override
            public Void call(Searcher searcher) throws Exception {
                checkin(searcher);
                return VOID;
            }
        };
    }

    private Function<Searcher> createNewLuceneSearcher() {
        return new Function<Searcher>() {
            @Override
            public Searcher call() throws Exception {
                return createLuceneSearcher();
            }
        };
    }

    private synchronized void checkin(Searcher searcher) throws IOException {
        PooledValue pooledValue = findPooledValue(searcher);
        int count = pooledValue.checkin();
        if(count == 0 && pooledValue.dirty()){
            closeAndRemove(pooledValue);
        }
    }

    private PooledValue findPooledValue(Searcher searcher) {
        return sequence(pool).find(where(PooledValue.searcher(), is(searcher))).get();
    }

    @Override
    public synchronized void markAsDirty() {
        sequence(pool).filter(where(theCheckoutCount(), is(0))).
                each(closeAndRemove());
        sequence(pool).filter(where(theCheckoutCount(), is(not(0)))).
                each(PooledValue.markAsDirty());
    }

    private Function1<PooledValue, Void> closeAndRemove() {
        return new Function1<PooledValue, Void>() {
            @Override
            public Void call(PooledValue pooledValue) throws Exception {
                closeAndRemove(pooledValue);
                return VOID;
            }
        };
    }

    private void closeAndRemove(PooledValue pooledValue) throws IOException {
        safeClose(pooledValue);
        pool.remove(pooledValue);
    }

    @Override
    public void close() throws IOException {
        for (PooledValue pooledValue : pool) {
            safeClose(pooledValue);
        }
        pool.clear();
    }

}
