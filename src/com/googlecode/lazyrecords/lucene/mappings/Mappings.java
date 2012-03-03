package com.googlecode.lazyrecords.lucene.mappings;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.lucene.Lucene;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Callables;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import static com.googlecode.lazyrecords.Record.functions.updateValues;
import static com.googlecode.lazyrecords.Record.methods.getKeyword;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class Mappings extends StringMappings {
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
                Keyword<?> keyword = getKeyword(name, definitions);
                return Pair.<Keyword<?>, Object>pair(keyword, toValue(keyword.forClass(), fieldable.stringValue()));
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
                Keyword<?> keyword = getKeyword(name, definitions);
                return new Field(name, Mappings.this.toString(keyword.forClass(), pair.second()), Field.Store.YES, Field.Index.NOT_ANALYZED);
            }
        };
    }

    public Function1<? super Record, Document> asDocument(final Definition definition) {
        return new Function1<Record, Document>() {
            public Document call(Record record) throws Exception {
                return record.fields().
                        add(Pair.<Keyword<?>, Object>pair(Lucene.RECORD_KEY, definition)).
                        map(asField(definition.fields())).
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
