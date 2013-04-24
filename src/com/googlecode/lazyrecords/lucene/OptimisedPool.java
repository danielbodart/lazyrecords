package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Function;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.googlecode.lazyrecords.lucene.PooledValue.checkoutValue;
import static com.googlecode.lazyrecords.lucene.PooledValue.isDirty;
import static com.googlecode.lazyrecords.lucene.PooledValue.theCheckoutCount;
import static com.googlecode.totallylazy.Closeables.safeClose;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

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

    @SuppressWarnings("deprecation")
    private LuceneSearcher createLuceneSearcher() throws IOException {
        return new LuceneSearcher(new IndexSearcher(DirectoryReader.open(directory)));
    }

    private Block<Searcher> checkIn() {
        return new Block<Searcher>() {
            @Override
            public void execute(Searcher searcher) throws Exception {
                checkin(searcher);
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

    private Block<PooledValue> closeAndRemove() {
        return new Block<PooledValue>() {
            @Override
            public void execute(PooledValue pooledValue) throws Exception {
                closeAndRemove(pooledValue);
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
