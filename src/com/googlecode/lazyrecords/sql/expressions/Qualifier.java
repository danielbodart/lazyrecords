package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.*;

import static com.googlecode.lazyrecords.sql.expressions.AnsiAsClause.asClause;
import static com.googlecode.totallylazy.Option.some;

public class Qualifier extends AbstractQualifier {
    private final String qualified;

    public Qualifier(String qualified) {
        this.qualified = qualified;
    }

    public SelectExpression qualify(SelectExpression expression) {
        return AnsiSelectExpression.selectExpression(expression.setQuantifier(), qualify(expression.selectList()),
                qualify(expression.fromClause()), qualify(expression.whereClause()), expression.orderByClause());
    }

    private FromClause qualify(FromClause fromClause) {
        return AnsiFromClause.fromClause(qualify(fromClause.tableReference()));
    }

    public TablePrimary qualify(TablePrimary tablePrimary) {
        return AnsiTablePrimary.tablePrimary(tablePrimary.tableName(), some(tablePrimary.asClause().getOrElse(asClause(qualified))));
    }

    public SelectList qualify(SelectList selectList) {
        return AnsiSelectList.selectList(qualify(selectList.derivedColumns()));
    }

    public WhereClause qualify(WhereClause whereClause) {
        return AnsiWhereClause.whereClause(qualify(whereClause.expression()));
    }

    public PredicateExpression qualify(PredicateExpression predicateExpression) {
        return AnsiPredicateExpression.predicateExpression(qualify(predicateExpression.predicand()), qualify(predicateExpression.predicate()));
    }

    public QualifiedJoin qualify(QualifiedJoin qualifiedJoin){
        return qualifiedJoin;
    }

    public CompoundExpression qualify(CompoundExpression compoundExpression) {
        return new CompoundExpression(qualify(compoundExpression.expressions()), compoundExpression.start(),
                compoundExpression.separator(), compoundExpression.end());
    }

    public DerivedColumn qualify(DerivedColumn derivedColumn) {
        return AnsiDerivedColumn.derivedColumn(qualify(derivedColumn.valueExpression()), derivedColumn.asClause(), derivedColumn.forClass());
    }

    public ColumnReference qualify(ColumnReference columnReference) {
        return ColumnReference.columnReference(columnReference.name(), some(columnReference.qualifier.getOrElse(qualified)));
    }

}
