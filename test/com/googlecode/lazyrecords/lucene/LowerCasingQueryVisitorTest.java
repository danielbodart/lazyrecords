package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Callable1;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import java.util.List;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class LowerCasingQueryVisitorTest {

    private final LowerCasingQueryVisitor visitor = new LowerCasingQueryVisitor();

    @Test
    public void shouldLowercaseTermQuery() throws Exception {
        final Query visited = visitor.visit(new TermQuery(new Term("field", "VaLue")));

        final Query expected = new TermQuery(new Term("field", "value"));
        assertThat(visited, is(expected));
    }

    @Test
    public void shouldLowercaseTermRangeQuery() throws Exception {
        final Query visited = visitor.visit(new TermRangeQuery("field", new BytesRef("LowerTerm"), new BytesRef("UpperTerm"), true, true));

        final Query expected = new TermRangeQuery("field", new BytesRef("lowerterm"), new BytesRef("upperterm"), true, true);
        assertThat(visited, is(expected));
    }

    @Test
    public void shouldKeepInclusionFlagsForTermRangeQuery() throws Exception {
        final TermRangeQuery visited = cast(visitor.visit(new TermRangeQuery("field", new BytesRef("LowerTerm"), new BytesRef("UpperTerm"), false, true)));

        assertThat(visited.includesLower(), is(false));
        assertThat(visited.includesUpper(), is(true));
    }

    @Test
    public void shouldLowercaseFuzzyQuery() throws Exception {
        final Query visited = visitor.visit(new FuzzyQuery(new Term("field", "VaLue")));

        final Query expected = new FuzzyQuery(new Term("field", "value"));
        assertThat(visited, is(expected));
    }

    @Test
    public void shouldLowercaseWildcardQuery() throws Exception {
        final Query visited = visitor.visit(new WildcardQuery(new Term("field", "VaLue")));

        final Query expected = new WildcardQuery(new Term("field", "value"));
        assertThat(visited, is(expected));
    }

    @Test
    public void shouldLowercaseAllTermsInAPhraseQuery() {
        final PhraseQuery query = new PhraseQuery();
        query.add(new Term("field", "VaLue1"));
        query.add(new Term("field", "vAlUE2"));
        final PhraseQuery visited = cast(visitor.visit(query));

        assertThat(sequence(visited.getTerms()), contains(new Term("field", "value1"), new Term("field", "value2")));
    }

    @Test
    public void shouldKeepSlopInAPhraseQuery() throws Exception {
        final PhraseQuery query = new PhraseQuery();
        query.add(new Term("field", "Value"));
        query.setSlop(3);

        final PhraseQuery visited = cast(visitor.visit(query));

        assertThat(visited.getSlop(), is(3));
    }

    @Test
    public void shouldLowercasePrefixQuery() throws Exception {
        final Query visited = visitor.visit(new PrefixQuery(new Term("field", "VaLue")));

        final Query expected = new PrefixQuery(new Term("field", "value"));
        assertThat(visited, is(expected));
    }

    @Test
    public void shouldLowercaseAllTermsInAMultiPhraseQuery() throws Exception {
        final MultiPhraseQuery query = new MultiPhraseQuery();
        query.add(new Term("field", "VaLuE1"));
        query.add(new Term[]{new Term("field", "VALUE2"), new Term("field", "vAlUe3")});

        final MultiPhraseQuery visited = cast(visitor.visit(query));

        assertThat(flatTerms(visited.getTermArrays()), contains(new Term("field", "value1"), new Term("field", "value2"), new Term("field", "value3")));
    }

    @Test
    public void shouldKeepSlopInAMultiPhraseQuery() throws Exception {
        final MultiPhraseQuery query = new MultiPhraseQuery();
        query.add(new Term("field", "Value"));
        query.setSlop(3);

        final MultiPhraseQuery visited = cast(visitor.visit(query));

        assertThat(visited.getSlop(), is(3));
    }

    private List<Term> flatTerms(List<Term[]> terms) {
        return sequence(terms).flatMap(new Callable1<Term[], Iterable<Term>>() {
            @Override
            public Iterable<Term> call(Term[] terms) throws Exception {
                return sequence(terms);
            }
        }).toList();
    }
}
