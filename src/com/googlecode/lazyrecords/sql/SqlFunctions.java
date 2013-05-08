package com.googlecode.lazyrecords.sql;

import com.googlecode.lazyrecords.Logger;
import com.googlecode.lazyrecords.Loggers;
import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Maps;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.Functions.constant;
import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Unchecked.cast;
import static java.lang.reflect.Proxy.newProxyInstance;

public class SqlFunctions {
    private final Connection connection;
    private final Logger logger;
    private Map<Class<?>, Integer> sqlTypeMap = new HashMap<Class<?>, Integer>() {{
        put(int.class, Types.INTEGER);
        put(String.class, Types.VARCHAR);
    }};

    public SqlFunctions(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public <T> T get(Class<T> sqlFunction) {
        return cast(newProxyInstance(getClass().getClassLoader(), new Class[]{sqlFunction}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                String call = String.format("{? = call %s%s}", functionName(method), arguments(method));
                final Map<String, Object> log = Maps.<String, Object>map(pair(Loggers.TYPE, Loggers.SQL), pair(Loggers.EXPRESSION, call));
                CallableStatement statement = connection.prepareCall(call);
                Object result = using(statement, new Callable1<CallableStatement, Object>() {
                    @Override
                    public Object call(CallableStatement statement) throws Exception {
                        statement.registerOutParameter(1, sqlTypeMap.get(method.getReturnType()));
                        for (int i = 0; i < args.length; i++) {
                            statement.setObject(i + 2, args[i], sqlTypeMap.get(method.getParameterTypes()[i]));
                        }
                        statement.execute();
                        return statement.getObject(1);
                    }
                });
                logger.log(log);
                return result;
            }
        }));
    }

    private String arguments(Method method) {
        return sequence(method.getParameterTypes()).map(constant("?")).toString("(", ",", ")");
    }

    private String functionName(Method method) {
        return method.getAnnotation(SqlFunction.class).value();
    }
}
