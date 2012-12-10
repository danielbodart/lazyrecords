package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Named;
import com.googlecode.lazyrecords.Record;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.comparators.AscendingComparator;
import com.googlecode.totallylazy.comparators.CompositeComparator;
import com.googlecode.totallylazy.comparators.DescendingComparator;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.util.Comparator;

import static com.googlecode.totallylazy.Sequences.sequence;

public class Sorting {
	public static Sort sort(Comparator<? super Record> comparator) {
		if (comparator instanceof AscendingComparator) {
			return sortBy(name(((AscendingComparator) comparator).callable()), false);
		}
		if (comparator instanceof DescendingComparator) {
			return sortBy(name(((DescendingComparator) comparator).callable()), true);
		}
		if (comparator instanceof CompositeComparator) {
			Sequence<SortField> sortFields = Unchecked.<CompositeComparator<? super Record>>cast(comparator).comparators().map(sort()).flatMap(sortFields());
			return new Sort(sortFields.toArray(SortField.class));
		}
		throw new UnsupportedOperationException("Unsupported comparator " + comparator);
	}

	private static Callable1<Sort, Iterable<SortField>> sortFields() {
		return new Callable1<Sort, Iterable<SortField>>() {
			@Override
			public Iterable<SortField> call(Sort sort) throws Exception {
				return sequence(sort.getSort());
			}
		};
	}

	private static Callable1<Comparator<? super Record>, Sort> sort() {
		return new Callable1<Comparator<? super Record>, Sort>() {
			@Override
			public Sort call(Comparator<? super Record> comparator) throws Exception {
				return sort(comparator);
			}
		};
	}

	private static Sort sortBy(String name, boolean reverse) {
		return new Sort(new SortField(name, SortField.STRING, reverse));
	}

	private static String name(Callable1<?, ?> callable) {
		if(callable instanceof Named){
			return ((Named) callable).name();
		}
		throw new UnsupportedOperationException("Unsupported reducer " + callable);
	}
}
