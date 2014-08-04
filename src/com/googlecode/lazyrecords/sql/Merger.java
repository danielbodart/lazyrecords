package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.sql.expressions.*;
import com.googlecode.totallylazy.*;

import java.util.Set;

import static com.googlecode.lazyrecords.sql.expressions.AnsiFromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.AnsiQualifiedJoin.qualifiedJoin;
import static com.googlecode.totallylazy.Sequences.*;

public class Merger {
    private final SelectExpression primary;
    private final SelectExpression secondary;
    private final JoinType joinType;
    private final JoinSpecification joinSpecification;
    private final Set<String> qualifiers;
    private final String primaryQualifier;
    private final String secondaryQualifier;

    Merger(final SelectExpression primary, final SelectExpression secondary, final JoinType joinType, final JoinSpecification joinSpecification) {
        TableReference primaryTable = primary.fromClause().tableReference();
        TableReference secondaryTable = secondary.fromClause().tableReference();
        qualifiers = qualifiers(primaryTable, secondaryTable);
        primaryQualifier = qualifier(primaryTable);
        secondaryQualifier = qualifier(secondaryTable);
        this.primary = Qualifier.qualifier(primaryQualifier).qualify(primary);
        this.secondary = Qualifier.qualifier(secondaryQualifier).qualify(secondary);
        this.joinType = joinType;
        this.joinSpecification = joinSpecification;
    }

    private String qualifier(final TableReference tableReference) {
        TablePrimary table = tables(tableReference).head();
        if (!table.asClause().isEmpty()) return table.asClause().get().alias();
        String prefix = String.valueOf(table.tableName().name().charAt(0)).toLowerCase();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String possible = i == 0 ? prefix : prefix + i;
            if (!qualifiers.contains(possible)) {
                qualifiers.add(possible);
                return possible;
            }
        }
        throw new UnsupportedOperationException();
    }

    private Sequence<TablePrimary> tables(final TableReference reference) {
        if (reference instanceof TablePrimary) return one((TablePrimary) reference);
        if (reference instanceof QualifiedJoin) {
            QualifiedJoin join = (QualifiedJoin) reference;
            return tables(join.left()).join(tables(join.right()));
        }
        throw new UnsupportedOperationException();
    }

    private Set<String> qualifiers(final TableReference... tables) {
        return sequence(tables).flatMap(new Mapper<TableReference, Iterable<String>>() {
            @Override
            public Iterable<String> call(final TableReference reference) throws Exception {
                return qualifiers(reference);
            }
        }).toSet();
    }

    private Iterable<String> qualifiers(TableReference reference) {
        if (reference instanceof TablePrimary) {
            TablePrimary tablePrimary = (TablePrimary) reference;
            return tablePrimary.asClause().isEmpty() ? empty(String.class) : one(tablePrimary.asClause().get().alias());
        }
        if (reference instanceof QualifiedJoin) {
            QualifiedJoin join = (QualifiedJoin) reference;
            return qualifiers(join.left(), join.right());
        }
        throw new UnsupportedOperationException();
    }

    public static Merger merger(final SelectExpression primary, final SelectExpression secondary, final JoinType joinType, final JoinSpecification joinSpecification) {
        return new Merger(primary, secondary, joinType, joinSpecification);
    }

    public SelectExpression merge() {
        return AnsiSelectExpression.selectExpression(
                primary.setQuantifier(),
                mergeSelectList(),
                mergeFromClause(),
                mergeWhereClause(),
                mergeOrderByClause(),
                mergeGroupByClause());
    }

    private Option<GroupByClause> mergeGroupByClause() {
        return sequence(primary.groupByClause(), secondary.groupByClause()).
                flatMap(identity(GroupByClause.class)).
                flatMap(GroupByClause.functions.groups).
                flatOption().
                map(AnsiGroupByClause.functions.groupByClause);
    }

    private Option<OrderByClause> mergeOrderByClause() {
        return sequence(primary.orderByClause(), secondary.orderByClause()).
                flatMap(identity(OrderByClause.class)).
                flatMap(OrderByClause.functions.sortSpecifications).
                flatOption().
                map(AnsiOrderByClause.functions.orderByClause);
    }

    private Option<WhereClause> mergeWhereClause() {
        return AnsiSelectBuilder.combine(primary.whereClause(), secondary.whereClause());
    }

    private FromClause mergeFromClause() {
        AnsiQualifiedJoin join = qualifiedJoin(primary.fromClause().tableReference(), joinType, secondary.fromClause().tableReference(), joinSpecification);
        return fromClause(new JoinQualifier(primaryQualifier, secondaryQualifier).qualify(join));
    }

    private SelectList mergeSelectList() {
        return AnsiSelectList.selectList(sequence(primary, secondary).flatMap(derivedColumns));
    }

    private static final Mapper<SelectExpression, Sequence<DerivedColumn>> derivedColumns = new Mapper<SelectExpression, Sequence<DerivedColumn>>() {
        @Override
        public Sequence<DerivedColumn> call(final SelectExpression expression) throws Exception {
            return expression.selectList().derivedColumns();
        }
    };
}
