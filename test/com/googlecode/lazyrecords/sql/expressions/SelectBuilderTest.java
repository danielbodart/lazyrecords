package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.RecordName;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.lazyrecords.Keyword;
import org.junit.Test;

import static com.googlecode.lazyrecords.Keywords.keyword;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SelectBuilderTest {
    private final RecordName cars = RecordName.recordName("cars");
    private final Keyword<String> make = keyword("make", String.class);
    private final Keyword<String> model = keyword("model", String.class);
    private final Keyword<Integer> one = keyword("1", Integer.class);

    @Test
    public void ifColumnsSelectedIsEmptyUseStar() throws Exception {
        SelectExpression build = from(cars).select(Sequences.<Keyword<?>>empty()).build();
        assertThat(build.text(), is("select * from cars"));
    }

    @Test
    public void selectASingleColumn() throws Exception {
        SelectExpression build = from(cars).select(make).build();
        assertThat(build.text(), is("select make from cars"));
    }

    @Test
    public void selectMultipleColumns() throws Exception {
        SelectExpression build = from(cars).select(make, model).build();
        assertThat(build.text(), is("select make,model from cars"));
    }

    @Test
    public void canBeUsedToTestForATable() throws Exception {
        SelectExpression build = from(cars).select(one).build();
        assertThat(build.text(), is("select 1 from cars"));
    }
}
