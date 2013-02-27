package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.sql.expressions.AnsiQualifiedJoin;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectBuilder;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectExpression;
import com.googlecode.lazyrecords.sql.expressions.AnsiSelectList;
import com.googlecode.lazyrecords.sql.expressions.DerivedColumn;
import com.googlecode.lazyrecords.sql.expressions.FromClause;
import com.googlecode.lazyrecords.sql.expressions.JoinQualifier;
import com.googlecode.lazyrecords.sql.expressions.JoinSpecification;
import com.googlecode.lazyrecords.sql.expressions.JoinType;
import com.googlecode.lazyrecords.sql.expressions.QualifiedJoin;
import com.googlecode.lazyrecords.sql.expressions.Qualifier;
import com.googlecode.lazyrecords.sql.expressions.SelectExpression;
import com.googlecode.lazyrecords.sql.expressions.SelectList;
import com.googlecode.lazyrecords.sql.expressions.TablePrimary;
import com.googlecode.lazyrecords.sql.expressions.TableReference;
import com.googlecode.lazyrecords.sql.expressions.WhereClause;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import java.util.Set;

import static com.googlecode.lazyrecords.sql.expressions.AnsiFromClause.fromClause;
import static com.googlecode.lazyrecords.sql.expressions.AnsiQualifiedJoin.qualifiedJoin;
import static com.googlecode.lazyrecords.sql.expressions.Qualifier.qualifier;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.Sequences.sequence;

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
                primary.orderByClause());
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
