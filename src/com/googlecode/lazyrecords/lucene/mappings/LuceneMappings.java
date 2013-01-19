package com.googlecode.lazyrecords.lucene.mappings;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.ToRecord;
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

import static com.googlecode.lazyrecords.Definition.methods.sortFields;
import static com.googlecode.lazyrecords.Record.functions.updateValues;
import static com.googlecode.totallylazy.Predicates.in;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class LuceneMappings {
    private final StringMappings stringMappings;

    public LuceneMappings(StringMappings stringMappings) {
        this.stringMappings = stringMappings;
    }

    public LuceneMappings() {
        this(new StringMappings());
    }

    public StringMappings stringMappings() {
        return stringMappings;
    }

    public ToRecord<Document> asRecord(final Sequence<Keyword<?>> definitions) {
        return new ToRecord<Document>() {
            public Record call(Document document) throws Exception {
                return sequence(document.getFields()).
                        map(asPair(definitions)).
                        filter(where(Callables.<Keyword<?>>first(), is(Predicates.<Keyword<?>>not(Lucene.RECORD_KEY)).and(in(definitions)))).
                        fold(SourceRecord.record(document), updateValues());
            }
        };
    }

    public Function1<Fieldable, Pair<Keyword<?>, Object>> asPair(final Sequence<Keyword<?>> definitions) {
        return new Function1<Fieldable, Pair<Keyword<?>, Object>>() {
            public Pair<Keyword<?>, Object> call(Fieldable fieldable) throws Exception {
                String name = fieldable.name();
                Keyword<?> keyword = Keyword.methods.matchKeyword(name, definitions);
                return Pair.<Keyword<?>, Object>pair(keyword, stringMappings.toValue(keyword.forClass(), fieldable.stringValue()));
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
                Keyword<?> keyword = Keyword.methods.matchKeyword(name, definitions);
                return new Field(name, LuceneMappings.this.stringMappings.toString(keyword.forClass(), pair.second()), Field.Store.YES, Field.Index.NOT_ANALYZED);
            }
        };
    }

    public RecordTo<Document> asDocument(final Definition definition) {
        return new RecordTo<Document>() {
            public Document call(Record record) throws Exception {
                return sortFields(definition, record).fields().
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
