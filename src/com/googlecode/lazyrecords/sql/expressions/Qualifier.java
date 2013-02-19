package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Unary;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.Unchecked;
import com.googlecode.totallylazy.annotations.multimethod;

import static com.googlecode.lazyrecords.sql.expressions.AnsiAsClause.asClause;
import static com.googlecode.totallylazy.Functions.constant;
import static com.googlecode.totallylazy.Option.some;

public class Qualifier extends AbstractQualifier {
    private final String tableAlias;
    private final UnaryFunction<String> qualified;

    protected Qualifier(final String tableAlias, Callable1<? super String, String> qualified) {
        this.tableAlias = tableAlias;
        this.qualified = Unary.constructors.unary(Unchecked.<Callable1<String, String>>cast(qualified));
    }

    public static Qualifier qualifier(final String name) {
        return qualifier(name, constant(name));
    }

    public static Qualifier qualifier(final String name, final Callable1<? super String, String> callable) {
        return new Qualifier(name, callable);
    }

    @multimethod public SelectExpression qualify(SelectExpression expression) {
        return AnsiSelectExpression.selectExpression(expression.setQuantifier(), qualify(expression.selectList()),
                qualify(expression.fromClause()), qualify(expression.whereClause()), expression.orderByClause());
    }

    @multimethod private FromClause qualify(FromClause fromClause) {
        return AnsiFromClause.fromClause(qualify(fromClause.tableReference()));
    }

    @multimethod public TablePrimary qualify(TablePrimary tablePrimary) {
        return AnsiTablePrimary.tablePrimary(tablePrimary.tableName(), some(tablePrimary.asClause().getOrElse(asClause(tableAlias))));
    }

    @multimethod public SelectList qualify(SelectList selectList) {
        return AnsiSelectList.selectList(qualify(selectList.derivedColumns()));
    }

    @multimethod public WhereClause qualify(WhereClause whereClause) {
        return AnsiWhereClause.whereClause(qualify(whereClause.expression()));
    }

    @multimethod public PredicateExpression qualify(PredicateExpression predicateExpression) {
        return AnsiPredicateExpression.predicateExpression(qualify(predicateExpression.predicand()), qualify(predicateExpression.predicate()));
    }

    @multimethod public QualifiedJoin qualify(QualifiedJoin qualifiedJoin) {
        return qualifiedJoin;
    }

    @multimethod public CompoundExpression qualify(CompoundExpression compoundExpression) {
        return new CompoundExpression(qualify(compoundExpression.expressions()), compoundExpression.start(),
                compoundExpression.separator(), compoundExpression.end());
    }

    @multimethod public DerivedColumn qualify(DerivedColumn derivedColumn) {
        return AnsiDerivedColumn.derivedColumn(qualify(derivedColumn.valueExpression()), derivedColumn.asClause(), derivedColumn.forClass());
    }

    @multimethod public ColumnReference qualify(ColumnReference columnReference) {
        return ColumnReference.columnReference(columnReference.name(), some(columnReference.qualifier.getOrElse(qualified.apply(columnReference.name()))));
    }
}
