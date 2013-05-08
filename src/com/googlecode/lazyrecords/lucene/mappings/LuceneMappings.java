package com.googlecode.lazyrecords.lucene.mappings;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.RecordTo;
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
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;

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

    public Function1<IndexableField, Pair<Keyword<?>, Object>> asPair(final Sequence<Keyword<?>> definitions) {
        return new Function1<IndexableField, Pair<Keyword<?>, Object>>() {
            public Pair<Keyword<?>, Object> call(IndexableField field) throws Exception {
                String name = field.name();
                Keyword<?> keyword = Keyword.methods.matchKeyword(name, definitions);
                return Pair.<Keyword<?>, Object>pair(keyword, stringMappings.toValue(keyword.forClass(), field.stringValue()));
            }
        };
    }

    public Function1<Pair<Keyword<?>, Object>, Field> asField(final Sequence<Keyword<?>> definitions) {
        return new Function1<Pair<Keyword<?>, Object>, Field>() {
            public Field call(Pair<Keyword<?>, Object> pair) throws Exception {
                if (pair.second() == null) {
                    return null;
                }

                String name = pair.first().name();
                Keyword<?> keyword = Keyword.methods.matchKeyword(name, definitions);
                return new StringField(name, LuceneMappings.this.stringMappings.toString(keyword.forClass(), pair.second()), Field.Store.YES);
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

    public static Function2<? super Document, ? super Field, Document> intoFields() {
        return new Function2<Document, Field, Document>() {
            public Document call(Document document, Field field) throws Exception {
                document.add(field);
                return document;
            }
        };
    }


}
