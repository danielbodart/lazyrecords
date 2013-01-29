package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.Definition;
import com.googlecode.lazyrecords.Grammar;
import com.googlecode.lazyrecords.sql.grammars.AnsiSqlGrammar;
import com.googlecode.lazyrecords.sql.grammars.SqlGrammar;
import com.googlecode.totallylazy.*;
import com.googlecode.lazyrecords.Keyword;
import org.junit.Test;

import static com.googlecode.lazyrecords.Definition.constructors.definition;
import static com.googlecode.lazyrecords.Grammar.where;
import static com.googlecode.lazyrecords.Keyword.constructors.keyword;
import static com.googlecode.lazyrecords.sql.expressions.SelectBuilder.from;
import static com.googlecode.totallylazy.Callables.when;
import static com.googlecode.totallylazy.Predicates.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SelectBuilderTest {
    private final Keyword<String> make = keyword("make", String.class);
    private final Keyword<String> model = keyword("model", String.class);
    private final Keyword<Integer> one = keyword("1", Integer.class);
    private final Definition cars = definition("cars", make, model, one);
    private final SqlGrammar grammar = new AnsiSqlGrammar();

    @Test
    public void ifColumnsSelectedIsEmptyUseDefinitionFields() throws Exception {
        Expression build = from(grammar, cars).select(Sequences.<Keyword<?>>empty()).build();
        assertThat(build.text(), is("select make, model, 1 from cars"));
    }

    @Test
    public void selectASingleColumn() throws Exception {
        Expression build = from(grammar, cars).select(make).build();
        assertThat(build.text(), is("select make from cars"));
    }

    @Test
    public void selectMultipleColumns() throws Exception {
        Expression build = from(grammar, cars).select(make, model).build();
        assertThat(build.text(), is("select make, model from cars"));
    }

    @Test
    public void canBeUsedToTestForATable() throws Exception {
        Expression build = from(grammar, cars).select(one).build();
        assertThat(build.text(), is("select 1 from cars"));
    }

    @Test
    public void canFlatternTheExpression() throws Exception {
        Expression expression = from(grammar, cars).select(make, model).distinct().where(where(make, Grammar.is("Honda"))).build();
        Sequence<Expression> original = expressions(expression);
        String alias = "t0";
        Sequence<Expression> expr = original.map(when(instanceOf(TableName.class), aliasTable(alias))).
                map(when(instanceOf(ColumnReference.class), qualifyColumn(alias)));
        System.out.println(expr);
    }

    private UnaryFunction<Expression> aliasTable(final String alias) {
        return new UnaryFunction<Expression>() {
            @Override
            public Expression call(Expression expression) throws Exception {
                return ((TableName) expression).alias(alias);
            }
        };
    }

    private UnaryFunction<Expression> qualifyColumn(final String name) {
        return new UnaryFunction<Expression>() {
            @Override
            public Expression call(Expression expression) throws Exception {
                return ((ColumnReference) expression).qualify(name);
            }
        };
    }

    private Sequence<Expression> expressions(Expression expression) {
        if(expression instanceof CompoundExpression){
            return expressions((CompoundExpression) expression);
        }
        return Sequences.one(expression);
    }

    private Sequence<Expression> expressions(CompoundExpression expression) {
        return expression.expressions.flatMap(new Mapper<Expression, Sequence<Expression>>() {
            @Override
            public Sequence<Expression> call(Expression expression) throws Exception {
                return expressions(expression);
            }
        });
    }

}
