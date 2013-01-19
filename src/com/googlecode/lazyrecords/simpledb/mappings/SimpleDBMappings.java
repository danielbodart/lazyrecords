package com.googlecode.lazyrecords.simpledb.mappings;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.googlecode.lazyrecords.RecordTo;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.ToRecord;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Unchecked;

import java.util.UUID;

import static com.googlecode.lazyrecords.SourceRecord.record;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.Predicates.notNullValue;
import static com.googlecode.totallylazy.Predicates.where;
import static com.googlecode.totallylazy.Sequences.sequence;

public class SimpleDBMappings {
    private final StringMappings stringMappings;

    public SimpleDBMappings(StringMappings stringMappings) {
        this.stringMappings = stringMappings;
    }

    public SimpleDBMappings() {
        this(new StringMappings());
    }

    public StringMappings stringMappings() {
        return stringMappings;
    }

    public ToRecord<Item> asRecord(final Sequence<Keyword<?>> definitions) {
        return new ToRecord<Item>() {
            public Record call(Item item) throws Exception {
                return sequence(item.getAttributes()).fold(record(item), asField(definitions));
            }
        };
    }

    public Function2<Record, Attribute, Record> asField(final Sequence<Keyword<?>> definitions) {
        return new Function2<Record, Attribute, Record>() {
            public Record call(Record mapRecord, Attribute attribute) throws Exception {
                Keyword<?> keyword = Keyword.methods.matchKeyword(attribute.getName(), definitions);
                return mapRecord.set(Unchecked.<Keyword<Object>>cast(keyword), stringMappings.toValue(keyword.forClass(), attribute.getValue()));
            }
        };
    }

    public RecordTo<ReplaceableItem> toReplaceableItem() {
        return new RecordTo<ReplaceableItem>() {
            public ReplaceableItem call(Record record) throws Exception {
                return new ReplaceableItem(UUID.randomUUID().toString(), record.fields().
                        filter(where(second(Object.class), is(notNullValue()))).
                        map(asAttribute()).toList());
            }
        };
    }

    public Function1<Pair<Keyword<?>, Object>, ReplaceableAttribute> asAttribute() {
        return new Function1<Pair<Keyword<?>, Object>, ReplaceableAttribute>() {
            public ReplaceableAttribute call(Pair<Keyword<?>, Object> pair) throws Exception {
                Keyword<?> keyword = pair.first();
                Object value = pair.second();
                String valueAsString = SimpleDBMappings.this.stringMappings.toString(Unchecked.<Class<Object>>cast(keyword.forClass()), value);
                return new ReplaceableAttribute(keyword.name(), valueAsString, true);
            }
        };
    }

}
