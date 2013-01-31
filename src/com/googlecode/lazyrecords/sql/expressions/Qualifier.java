package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.UnaryFunction;
import com.googlecode.totallylazy.multi;

public class Qualifier {
    private final String qualified;

    public Qualifier(String qualified) {
        this.qualified = qualified;
    }

    public SelectExpression qualify(SelectExpression expression) {
        return AnsiSelectExpression.selectExpression(expression.setQuantifier(), qualify(expression.selectList()), qualify(expression.fromClause()), expression.whereClause(), expression.orderByClause());

    }

    private FromClause qualify(FromClause fromClause) {
        return AnsiFromClause.fromClause(qualify(fromClause.tableReference()));
    }

    public TableReference qualify(TableReference tableReference) {
        return AnsiTableReference.tableReference(tableReference.tableName().qualify(qualified), tableReference.asClause());
    }

    public SelectList qualify(SelectList selectList) {
        return AnsiSelectList.selectList(qualify(selectList.derivedColumns()));
    }

    public Sequence<DerivedColumn> qualify(Sequence<DerivedColumn> derivedColumns) {
        return derivedColumns.map(new UnaryFunction<DerivedColumn>() {
            @Override
            public DerivedColumn call(DerivedColumn derivedColumn) throws Exception {
                return qualify(derivedColumn);
            }
        });
    }

    public DerivedColumn qualify(DerivedColumn derivedColumn) {
        return AnsiDerivedColumn.derivedColumn(qualifyValue(derivedColumn.valueExpression()), derivedColumn.asClause());
    }

    public ValueExpression qualifyValue(ValueExpression valueExpression) {
        return new multi(){}.<ValueExpression>methodOption(valueExpression).getOrElse(valueExpression);
    }

    public ColumnReference qualifyValue(ColumnReference columnReference) {
        return ColumnReference.columnReference(columnReference.name(), Option.some(qualified));
    }
}
