package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.lazyrecords.sql.mappings.SqlMapping;
import com.googlecode.lazyrecords.sql.mappings.SqlMappings;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.LazyException;
import com.googlecode.totallylazy.Maps;
import com.googlecode.totallylazy.Option;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Map;

import static com.googlecode.lazyrecords.sql.SqlFunction.functions.value;
import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Functions.constant;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static com.googlecode.totallylazy.callables.TimeCallable.calculateMilliseconds;
import static java.lang.reflect.Proxy.newProxyInstance;

public class SqlFunctions {
    private final Connection connection;
    private final SqlMappings mappings;
    private final Logger logger;

    public SqlFunctions(Connection connection, SqlMappings mappings, Logger logger) {
        this.connection = connection;
        this.mappings = mappings;
        this.logger = logger;
    }

    public <T> T get(Class<T> sqlFunction) {
        return cast(newProxyInstance(getClass().getClassLoader(), new Class[]{sqlFunction}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                String call = buildCall(method);
                final Map<String, Object> log = Maps.<String, Object>map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.EXPRESSION, call));
                long start = System.nanoTime();

                try {
                    return using(connection.prepareCall(call), callStatement(method, args));
                } catch (Exception e) {
                    log.put(Loggers.MESSAGE, e.getMessage());
                    throw LazyException.lazyException(e);
                } finally {
                    log.put(Loggers.MILLISECONDS, calculateMilliseconds(start, System.nanoTime()));
                    logger.log(log);
                }
            }
        }));
    }

    private Callable1<CallableStatement, Object> callStatement(final Method method, final Object[] args) {
        return new Callable1<CallableStatement, Object>() {
            @Override
            public Object call(CallableStatement statement) throws Exception {
                final Class<?> returnType = method.getReturnType();
                final SqlMapping<Object> returnValueMapping = mappings.get(returnType);
                statement.registerOutParameter(1, returnValueMapping.sqlType());
                for (int i = 0; i < (args == null ? 0 : args.length); i++) {
                    statement.setObject(i + 2, args[i], mappings.get(method.getParameterTypes()[i]).sqlType());
                }
                statement.execute();
                return returnValueMapping.getValue(statement, 1);
            }
        };
    }

    private String buildCall(Method method) {
        return String.format("{? = call %s%s}", functionName(method), arguments(method.getParameterTypes()));
    }

    private String arguments(Class<?>[] parameterTypes) {
        return parameterTypes.length == 0 ? "" : sequence(parameterTypes).map(constant("?")).toString("(", ",", ")");
    }

    private String functionName(Method method) {
        return option(method.getAnnotation(SqlFunction.class)).map(value()).getOrElse(method.getName());
    }
}
