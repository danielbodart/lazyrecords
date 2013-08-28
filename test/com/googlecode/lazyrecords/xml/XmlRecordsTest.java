package com.googlecode.lazyrecords.xml;

import com.googlecode.lazyrecords.AliasedKeyword;
import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Keywords;
import com.googlecode.lazyrecords.Records;
import com.googlecode.lazyrecords.RecordsContract;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Xml;
import com.googlecode.totallylazy.matchers.NumberMatcher;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.totallylazy.Xml.document;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class XmlRecordsTest extends RecordsContract<XmlRecords> {
    private Node root;

    @Override
    protected XmlRecords createRecords() throws Exception {
        root = document("<data/>");
        return new XmlRecords(root);
    }

    @Test
    public void showGeneratedXml() throws Exception {
        System.out.println(Xml.format(root));
    }

    @Test
    public void shouldSupportAliasingByGetting() throws Exception {
        Records xmlRecords = new XmlRecords(document("<data><user><summary><firstName>Dan</firstName></summary></user></data>"));

        Keyword<String> aliased = keyword("summary/firstName", String.class).as("first").metadata(Keywords.indexed, true);
        Definition definition = definition("/data/user", aliased);
        Sequence<Pair<Keyword<?>,Object>> fields = xmlRecords.get(definition).head().fields();
        assertThat(fields.size(), NumberMatcher.is(1));
        Pair<Keyword<?>, Object> pair = fields.head();
        Keyword<?> keyword = pair.first();
        assertThat(keyword.name(), is("first"));
        assertThat(keyword.forClass().equals(String.class), is(true));
        assertThat(keyword, is(not(instanceOf(AliasedKeyword.class))));
        assertThat(keyword.metadata().fields().size(), NumberMatcher.is(1));
        assertThat(keyword.metadata().get(Keywords.indexed), is(true));
    }

    @Override
    @Ignore
    public void putDoesntRemoveOtherFields() throws Exception {
        //not yet implemented
    }
}
