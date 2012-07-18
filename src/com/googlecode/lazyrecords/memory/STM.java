package com.googlecode.lazyrecords.memory;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.totallylazy.Atomic;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.collections.ImmutableList;
import com.googlecode.totallylazy.collections.ImmutableMap;
import com.googlecode.totallylazy.collections.ImmutableSortedMap;

import java.util.Comparator;

import static com.googlecode.totallylazy.Atomic.constructors.atomic;

public class STM implements Atomic<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> {
    private final Atomic<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> value;

    public STM(ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> value) {
        this.value = atomic(value);
    }

    public STM() {
        this(ImmutableSortedMap.constructors.<Definition, ImmutableList<ImmutableMap<String, String>>>sortedMap(definitionComparator));
    }

    @Override
    public Atomic<ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> modify(Callable1<? super ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>, ? extends ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>>> callable) {
        return value.modify(callable);
    }

    @Override
    public ImmutableMap<Definition, ImmutableList<ImmutableMap<String, String>>> value() {
        return value.value();
    }

    public STM snapshot() {
        return new STM(value.value());
    }

    private static final Comparator<Definition> definitionComparator = new Comparator<Definition>() {
        @Override
        public int compare(Definition a, Definition b) {
            return a.name().compareTo(b.name());
        }
    };
}
