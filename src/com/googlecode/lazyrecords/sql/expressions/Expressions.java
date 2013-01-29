package com.googlecode.lazyrecords.sql.expressions;

import com.googlecode.lazyrecords.*;
import com.googlecode.totallylazy.*;
import com.googlecode.totallylazy.regex.Regex;

import static com.googlecode.lazyrecords.sql.expressions.ColumnReference.columnName;
import static com.googlecode.lazyrecords.sql.expressions.SelectList.asClause;
import static com.googlecode.lazyrecords.sql.expressions.TableName.tableName;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class Expressions {
    public static Function1<? super Expression, Iterable<Object>> parameters() {
        return new Function1<Expression, Iterable<Object>>() {
            public Iterable<Object> call(Expression expression) throws Exception {
                return expression.parameters();
            }
        };
    }

    public static Function1<? super Expression, String> text() {
        return new Function1<Expression, String>() {
            public String call(Expression expression) throws Exception {
                return expression.text();
            }
        };
    }

    public static TextOnlyExpression textOnly(String expression, Object... args) {
        return textOnly(format(expression, args));
    }

    public static TextOnlyExpression textOnly(Object expression) {
        return TextOnlyExpression.textOnly(expression.toString());
    }

    public static ColumnReference name(Keyword<?> named) {
        ColumnReference quoted = columnName(named.name());
        ColumnReference qualified = metadata(named, Keywords.qualifier).fold(quoted, new Function2<ColumnReference, String, ColumnReference>() {
            @Override
            public ColumnReference call(ColumnReference expression, String qualifier) throws Exception {
                return expression.qualify(qualifier);
            }
        });
        return metadata(named, Keywords.alias).fold(qualified, new Function2<ColumnReference, String, ColumnReference>() {
            @Override
            public ColumnReference call(ColumnReference expression, String alias) throws Exception {
                return expression.alias(alias);
            }
        });
    }

    public static TableName name(Definition definition) {
        TableName quoted = tableName(definition.name());
        return metadata(definition, Keywords.alias).fold(quoted, new Function2<TableName, String, TableName>() {
            @Override
            public TableName call(TableName expression, String alias) throws Exception {
                return expression.alias(alias);
            }
        });
    }

    private static <T> Option<T> metadata(Named named, Keyword<T> keyword) {
        if (named instanceof Metadata) {
            return ((Metadata) named).metadata().getOption(keyword);
        }
        return Option.none();
    }

    public static Mapper<Keyword<?>, AbstractExpression> name() {
        return new Mapper<Keyword<?>, AbstractExpression>() {
            @Override
            public AbstractExpression call(Keyword<?> keyword) throws Exception {
                return name(keyword);
            }
        };
    }

    public static String names(Sequence<Keyword<?>> keywords) {
        return formatList(keywords.map(name()));
    }

    public static String formatList(final Sequence<?> values) {
        return values.toString("(", ",", ")");
    }

    private static final Regex legal = Regex.regex("[a-zA-Z0-9_$*#.@]+");

    public static String quote(String name) {
        if (Strings.isEmpty(name)) return name;
        if (legal.matches(name)) return name;
        return '"' + name + '"';
    }

    public static AbstractExpression expression(String expression, Object head, Object... tail) {
        return new TextAndParametersExpression(expression, sequence(tail).cons(head));
    }

    public static AbstractExpression expression(String expression, Sequence<Object> parameters) {
        if (parameters.isEmpty()) {
            return textOnly(expression);
        }
        return new TextAndParametersExpression(expression, parameters);
    }

    public static EmptyExpression empty() {
        return new EmptyExpression();
    }

    public static CompoundExpression join(final Expression... expressions) {
        return new CompoundExpression(expressions);
    }

    public static CompoundExpression join(final Sequence<? extends Expression> expressions) {
        return new CompoundExpression(expressions);
    }

    public static CompoundExpression join(final Sequence<? extends Expression> expressions, final String start, final String separator, final String end) {
        return new CompoundExpression(expressions, start, separator, end);
    }

    public static boolean isEmpty(Expression expression) {
        return empty().text().equals(expression.text());
    }

    public static String toString(Expression expression, Callable1<Object, Object> valueConverter) {
        return format(expression.text().replace("%", "%%").replace("?", "'%s'"), expression.parameters().map(valueConverter).toArray(Object.class));
    }
}
