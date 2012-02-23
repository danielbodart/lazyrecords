package com.googlecode.lazyrecords.lucene.mappings;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordMethods;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.lucene.Lucene;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.lazyrecords.RecordMethods.updateValues;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Mappings {
    private final Map<Class<?>, Mapping<Object>> map = new HashMap<Class<?>, Mapping<Object>>();

    public Mappings() {
        add(Date.class, new DateMapping());
        add(Integer.class, new NumericIntegerMapping());
        add(Long.class, new NumericLongMapping());
        add(String.class, new StringMapping());
        add(URI.class, new UriMapping());
        add(Boolean.class, new BooleanMapping());
        add(UUID.class, new UUIDMapping());
        add(Object.class, new ObjectMapping());
    }

    public <T> Mappings add(final Class<T> type, final Mapping<T> mapping) {
        map.put(type, Unchecked.<Mapping<Object>>cast(mapping));
        return this;
    }

    public Mapping<Object> get(final Class<?> aClass) {
        if (!map.containsKey(aClass)) {
            return (Mapping<Object>) map.get(Object.class);
        }
        return (Mapping<Object>) map.get(aClass);
    }

    public Function1<Document, Record> asRecord(final Sequence<Keyword<?>> definitions) {
        return new Function1<Document, Record>() {
            public Record call(Document document) throws Exception {
                return sequence(document.getFields()).
                        map(asPair(definitions)).
                        filter(where(Callables.<Keyword<?>>first(), is(Predicates.<Keyword<?>>not(Lucene.RECORD_KEY)))).
                        fold(new SourceRecord<Document>(document), updateValues());
            }
        };
    }

    public Function1<Fieldable, Pair<Keyword<?>, Object>> asPair(final Sequence<Keyword<?>> definitions) {
        return new Function1<Fieldable, Pair<Keyword<?>, Object>>() {
            public Pair<Keyword<?>, Object> call(Fieldable fieldable) throws Exception {
                String name = fieldable.name();
                Keyword<?> keyword = RecordMethods.getKeyword(name, definitions);
                return Pair.<Keyword<?>, Object>pair(keyword, get(keyword.forClass()).toValue(fieldable));
            }
        };
    }

    public Function1<Pair<Keyword<?>, Object>, Fieldable> asField(final Sequence<Keyword<?>> definitions) {
        return new Function1<Pair<Keyword<?>, Object>, Fieldable>() {
            public Fieldable call(Pair<Keyword<?>, Object> pair) throws Exception {
                if (pair.second() == null) {
                    return null;
                }

                String name = pair.first().name();
                Keyword<?> keyword = RecordMethods.getKeyword(name, definitions);
                return get(keyword.forClass()).toField(name, pair.second());
            }
        };
    }

    public Function1<? super Record, Document> asDocument(final Definition definition, final Sequence<Keyword<?>> definitions) {
        return new Function1<Record, Document>() {
            public Document call(Record record) throws Exception {
                return record.fields().
                        add(Pair.<Keyword<?>, Object>pair(Lucene.RECORD_KEY, definition)).
                        map(asField(definitions)).
                        filter(notNullValue()).
                        fold(new Document(), intoFields());
            }
        };
    }

    public static Function2<? super Document, ? super Fieldable, Document> intoFields() {
        return new Function2<Document, Fieldable, Document>() {
            public Document call(Document document, Fieldable fieldable) throws Exception {
                document.add(fieldable);
                return document;
            }
        };
    }


}
