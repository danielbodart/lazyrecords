package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Xml;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.SourceRecord;
import com.googlecode.lazyrecords.xml.mappings.Mappings;
import org.w3c.dom.Node;

import java.util.Iterator;

public class XmlSequence extends Sequence<Record> {
    private final Sequence<Node> nodes;
    private final Mappings mappings;
    private final Sequence<Keyword<?>> definitions;

    public XmlSequence(Sequence<Node> nodes, Mappings mappings, Sequence<Keyword<?>> definitions) {
        this.nodes = nodes;
        this.mappings = mappings;
        this.definitions = definitions;
    }

    public Iterator<Record> iterator() {
        return nodes.map(asRecord()).iterator();
    }

    private Function1<? super Node, Record> asRecord() {
        return new Function1<Node, Record>() {
            public Record call(final Node node) throws Exception {
                return definitions.fold(new SourceRecord<Node>(node), populateFrom(node));
            }
        };
    }

    private Callable2<Record, Keyword<?>, Record> populateFrom(final Node node) {
        return new Callable2<Record, Keyword<?>, Record>() {
            public Record call(Record nodeRecord, Keyword<?> keyword) throws Exception {
                Sequence<Node> nodes = Xml.selectNodes(node, xpath(keyword));
                if (nodes.isEmpty()) {
                    return nodeRecord;
                }
                Object value = mappings.get(keyword.forClass()).from(nodes);
                return nodeRecord.set(Keywords.keyword(keyword), value);
            }
        };
    }

    private String xpath(Keyword<?> keyword) {
        if(keyword instanceof AliasedKeyword){
            return ((AliasedKeyword) keyword).source().name();
        }
        return keyword.name();
    }
}
