package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.InnerJoin;
import com.googlecode.lazyrecords.Join;
import com.googlecode.lazyrecords.Joiner;
import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.On;
import com.googlecode.lazyrecords.OuterJoin;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.Using;
import com.googlecode.lazyrecords.sql.expressions.AnsiJoinType;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectExpression;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectList;
import com.googlecode.lazyrecords.sql.expressions.DerivedColumn;
import com.googlecode.lazyrecords.sql.expressions.Expressible;
import com.googlecode.lazyrecords.sql.expressions.ExpressionBuilder;
import com.googlecode.lazyrecords.sql.expressions.FromClause;
import com.googlecode.lazyrecords.sql.expressions.JoinCondition;
import com.googlecode.lazyrecords.sql.expressions.JoinQualifier;
import com.googlecode.lazyrecords.sql.expressions.JoinSpecification;
import com.googlecode.lazyrecords.sql.expressions.JoinType;
import com.googlecode.lazyrecords.sql.expressions.NamedColumnsJoin;
import com.googlecode.lazyrecords.sql.expressions.Qualifier;
import com.googlecode.lazyrecords.sql.expressions.SelectBuilder;
import com.googlecode.lazyrecords.sql.expressions.SelectExpression;
import com.googlecode.lazyrecords.sql.expressions.SelectList;
import com.googlecode.lazyrecords.sql.expressions.TableReference;
import com.googlecode.totallylazy.Function2;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Quadruple;
import com.googlecode.totallylazy.Reducer;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Triple;
import com.googlecode.totallylazy.multi;

import java.util.Comparator;

import static com.googlecode.lazyrecords.sql.expressions.AnsiFromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.AnsiQualifiedJoin.qualifiedJoin;
import static com.googlecode.lazyrecords.sql.expressions.Expressions.columnReference;
import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Callables.second;
import static com.googlecode.totallylazy.Callables.third;
import static com.googlecode.totallylazy.Sequences.cons;
import static com.googlecode.totallylazy.Sequences.iterate;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Triple.triple;
import static com.googlecode.totallylazy.numbers.Numbers.increment;

public class JoinBuilder implements ExpressionBuilder {
    public static final String primaryQualified = "p";
    public static final String secondaryQualifier = "s";
    private final SelectBuilder primary;
    private final Sequence<Triple<ExpressionBuilder, JoinType, JoinSpecification>> secondaries;

    private JoinBuilder(SelectBuilder primary, Sequence<Triple<ExpressionBuilder, JoinType, JoinSpecification>> secondaries) {
        this.primary = primary;
        this.secondaries = secondaries;
    }

    public static JoinBuilder join(ExpressionBuilder primary, ExpressionBuilder secondary, JoinType type, JoinSpecification specification) {
        return new multi() {
        }.method(primary, secondary, type, specification);
    }

    public static JoinBuilder join(SelectBuilder builder, ExpressionBuilder secondary, JoinType type, JoinSpecification specification) {
        return join(builder, one(triple(secondary, type, specification)));
    }

    public static JoinBuilder join(SelectBuilder primary, Sequence<Triple<ExpressionBuilder, JoinType, JoinSpecification>> secondaries) {
        return new JoinBuilder(primary, secondaries);
    }

    public static JoinBuilder join(JoinBuilder builder, ExpressionBuilder secondary, JoinType type, JoinSpecification specification) {
        return join(builder.primary, builder.secondaries.add(triple(secondary, type, specification)));
    }

    public static ExpressionBuilder join(final ExpressionBuilder builder, final Join join) {
        ExpressionBuilder secondary = (ExpressionBuilder) ((Expressible) join.records()).build();
        JoinType type = joinType(join);
        JoinSpecification specification = joinSpecification(join.joiner());
        return join(builder, secondary, type, specification);
    }

    private static JoinSpecification joinSpecification(final Joiner joiner) {
        if (joiner instanceof On<?>)
            return JoinCondition.joinCondition(columnReference(((On) joiner).left()), columnReference(((On) joiner).right()));
        if (joiner instanceof Using)
            return NamedColumnsJoin.namedColumnsJoin(((Using) joiner).keywords().map(Keyword.functions.name));
        throw new UnsupportedOperationException();
    }

    private static JoinType joinType(final Join join) {
        if (join instanceof InnerJoin) return AnsiJoinType.inner;
        if (join instanceof OuterJoin) return AnsiJoinType.left;
        throw new UnsupportedOperationException();
    }


    @Override
    public Sequence<Keyword<?>> fields() {
        return primary.fields().
                join(secondaries.
                        map(first(ExpressionBuilder.class)).
                        flatMap(fields)).
                reverse().
                unique();
    }

    private static final Mapper<ExpressionBuilder, Sequence<Keyword<?>>> fields = new Mapper<ExpressionBuilder, Sequence<Keyword<?>>>() {
        @Override
        public Sequence<Keyword<?>> call(final ExpressionBuilder builder) throws Exception {
            return builder.fields();
        }
    };

    @Override
    public ExpressionBuilder select(Keyword<?>... columns) {
        return select(sequence(columns));
    }

    @Override
    public ExpressionBuilder select(Sequence<? extends Keyword<?>> columns) {
        return join(primary.select(columns), secondaries);
    }

    @Override
    public ExpressionBuilder filter(Predicate<? super Record> predicate) {
        return join(primary.filter(predicate), secondaries);
    }

    @Override
    public ExpressionBuilder orderBy(Comparator<? super Record> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder distinct() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionBuilder reduce(Reducer<?, ?> reducer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SelectExpression build() {
        SelectExpression qualifiedPrimary = new Qualifier(primaryQualified).qualify(primary.build());
        Sequence<SelectExpression> qualifiedSecondaries = secondaries.map(first(ExpressionBuilder.class)).
                map(selectExpression).
                zipWithIndex().
                map(qualify).
                realise();
        return AnsiSelectExpression.selectExpression(
                qualifiedPrimary.setQuantifier(),
                mergeSelectList(qualifiedPrimary, qualifiedSecondaries),
                mergeFromClause(qualifiedPrimary, qualifiedSecondaries),
                qualifiedPrimary.whereClause(),
                qualifiedPrimary.orderByClause());
    }

    private FromClause mergeFromClause(final SelectExpression qualifiedPrimary, final Sequence<SelectExpression> qualifiedSecondaries) {
        return fromClause(secondaries.map(second(JoinType.class)).zip(iterate(increment(), 0), secondaries.map(third(JoinSpecification.class)), qualifiedSecondaries.map(tableReference)).
                fold(qualifiedPrimary.fromClause().tableReference(), new Function2<TableReference, Quadruple<JoinType, Number, JoinSpecification, TableReference>, TableReference>() {
                    @Override
                    public TableReference call(final TableReference reference, final Quadruple<JoinType, Number, JoinSpecification, TableReference> quadruple) throws Exception {
                        return new JoinQualifier(primaryQualified, secondaryQualifier + quadruple.second()).qualify(qualifiedJoin(reference, quadruple.first(), quadruple.fourth(), quadruple.third()));
                    }
                }));
    }

    private static final Mapper<SelectExpression, TableReference> tableReference = new Mapper<SelectExpression, TableReference>() {
        @Override
        public TableReference call(final SelectExpression expression) throws Exception {
            return expression.fromClause().tableReference();
        }
    };

    private static SelectList mergeSelectList(final SelectExpression select, final Sequence<SelectExpression> expressions) {
        return AnsiSelectList.selectList(cons(select, expressions).flatMap(derivedColumns));
    }

    private static final Mapper<SelectExpression, Sequence<DerivedColumn>> derivedColumns = new Mapper<SelectExpression, Sequence<DerivedColumn>>() {
        @Override
        public Sequence<DerivedColumn> call(final SelectExpression expression) throws Exception {
            return expression.selectList().derivedColumns();
        }
    };

    private static final Mapper<Pair<Number, SelectExpression>, SelectExpression> qualify = new Mapper<Pair<Number, SelectExpression>, SelectExpression>() {
        @Override
        public SelectExpression call(final Pair<Number, SelectExpression> pair) throws Exception {
            return new Qualifier(secondaryQualifier + pair.first()).qualify(pair.second());
        }
    };

    private static final Mapper<ExpressionBuilder, SelectExpression> selectExpression = new Mapper<ExpressionBuilder, SelectExpression>() {
        @Override
        public SelectExpression call(final ExpressionBuilder expressionBuilder) throws Exception {
            return (SelectExpression) expressionBuilder.build();
        }
    };

    @Override
    public String text() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<Object> parameters() {
        throw new UnsupportedOperationException();
    }

}
