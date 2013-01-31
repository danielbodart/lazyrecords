package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.totallylazy.*;

import static com.googlecode.totallylazy.Unchecked.cast;

public class Qualifier {
    private final String qualified;

    public Qualifier(String qualified) {
        this.qualified = qualified;
    }

    public <T extends Expression> T qualify(final T expression) {
        return new multi() {}.<T>methodOption(expression).getOrElse(new Function<T>() {
            @Override
            public T call() throws Exception {
                if(expression instanceof CompoundExpression) return cast(compound((CompoundExpression) expression));
                return expression;
            }
        });
    }

    public SelectExpression qualify(SelectExpression expression) {
        return AnsiSelectExpression.selectExpression(expression.setQuantifier(), qualify(expression.selectList()),
                qualify(expression.fromClause()), qualify(expression.whereClause()), expression.orderByClause());
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

    public WhereClause qualify(WhereClause whereClause) {
        return AnsiWhereClause.whereClause(qualify(whereClause.expression()));
    }

    public PredicateExpression qualify(PredicateExpression predicateExpression) {
        return AnsiPredicateExpression.predicateExpression(qualify(predicateExpression.predicand()), qualify(predicateExpression.predicate()));
    }

    public CompoundExpression compound(CompoundExpression compoundExpression) {
        return new CompoundExpression(qualify(compoundExpression.expressions()), compoundExpression.start(), compoundExpression.separator(), compoundExpression.end());
    }

    public DerivedColumn qualify(DerivedColumn derivedColumn) {
        return AnsiDerivedColumn.derivedColumn(qualify(derivedColumn.valueExpression()), derivedColumn.asClause());
    }

    public ColumnReference qualify(ColumnReference columnReference) {
        return ColumnReference.columnReference(columnReference.name(), Option.some(qualified));
    }

    public  <T extends Expression, M extends Functor<T>> M qualify(M items) {
        return cast(items.map(this.<T>qualify()));
    }

    private <T extends Expression> UnaryFunction<T> qualify() {
        return new UnaryFunction<T>() {
            @Override
            public T call(T expression) throws Exception {
                return qualify(expression);
            }
        };
    }

}
