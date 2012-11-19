package com.googlecode.lazyrecords.sql.grammars;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ColumnDatatypeMappings {

	public static Map<Class, String> mysql() {
		Map<Class, String> map = defaultMappings();
		map.put(Date.class, "datetime");
		return map;
	}

	public static Map<Class, String> oracle() {
		return defaultMappings();
	}

	public static Map<Class, String> hsql() {
		return defaultMappings();
	}

	public static Map<Class, String> defaultMappings() {
		return new ConcurrentHashMap<Class, String>() {{
			put(BigDecimal.class, "decimal(20,6)");
			put(Date.class, "timestamp");
			put(Integer.class, "integer");
			put(Long.class, "bigint");
			put(Timestamp.class, "timestamp");
			put(Boolean.class, "varchar(5)");
			put(UUID.class, "varchar(36)");
			put(URI.class, "varchar(4000)");
			put(String.class, "varchar(4000)");
			put(Object.class, "clob");
		}};
	}
}
