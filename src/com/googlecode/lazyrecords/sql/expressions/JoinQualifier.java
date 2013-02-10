package com.googlecode.lazyrecords.sql.expressions;

public class JoinQualifier extends AbstractQualifier {
    private final Qualifier left;
    private final Qualifier right;

    public JoinQualifier(final String left, final String right) {
        this.left = Qualifier.qualifier(left);
        this.right = Qualifier.qualifier(right);
    }

    public QualifiedJoin qualify(final QualifiedJoin qualifiedJoin) {
        return AnsiQualifiedJoin.qualifiedJoin(left.qualify(qualifiedJoin.left()), qualifiedJoin.joinType(),
                right.qualify(qualifiedJoin.right()), qualify(qualifiedJoin.joinSpecification()));
    }

    public JoinCondition qualify(JoinCondition joinCondition){
        return JoinCondition.joinCondition(left.qualify(joinCondition.left()), right.qualify(joinCondition.right()));
    }
}
