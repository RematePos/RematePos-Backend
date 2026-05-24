package com.corhuila.microservices.customer_microservice;

import com.mongodb.ConnectionString;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerMicroserviceApplication {

	public static void main(String[] args) {
		applyMongoUriEnvironmentFallback(args);
		SpringApplication.run(CustomerMicroserviceApplication.class, args);
	}

	private static void applyMongoUriEnvironmentFallback(String[] args) {
		var explicitMongoUri = firstNonBlank(
				System.getenv("SPRING_DATA_MONGODB_URI"),
				System.getenv("SPRING_MONGODB_URI")
		);

		if (isCloudProfile(args) && explicitMongoUri == null) {
			throw new IllegalStateException(
					"Customer Mongo URI is required in cloud. Configure SPRING_DATA_MONGODB_URI or SPRING_MONGODB_URI."
			);
		}

		if (explicitMongoUri != null && !hasMongoScheme(explicitMongoUri)) {
			throw new IllegalStateException(
					"Customer Mongo URI is invalid. It must start with mongodb:// or mongodb+srv://."
			);
		}

		if (explicitMongoUri != null && isBlank(System.getProperty("spring.data.mongodb.uri"))) {
			System.setProperty("spring.data.mongodb.uri", normalizeMongoUri(explicitMongoUri));
		}

		var databaseName = firstNonBlank(System.getenv("CUSTOMER_DB_NAME"), "customer_db");
		System.setProperty("spring.data.mongodb.database", databaseName);
	}

	private static boolean isCloudProfile(String[] args) {
		var profiles = firstNonBlank(
				System.getenv("SPRING_PROFILES_ACTIVE"),
				System.getProperty("spring.profiles.active"),
				findArgumentValue(args, "--spring.profiles.active=")
		);

		if (profiles == null) {
			return false;
		}

		for (String profile : profiles.split(",")) {
			if ("cloud".equals(profile.trim())) {
				return true;
			}
		}
		return false;
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (!isBlank(value)) {
				return value;
			}
		}
		return null;
	}

	private static String findArgumentValue(String[] args, String prefix) {
		for (String arg : args) {
			if (arg != null && arg.startsWith(prefix)) {
				return arg.substring(prefix.length());
			}
		}
		return null;
	}

	private static boolean hasMongoScheme(String value) {
		return value.startsWith("mongodb://") || value.startsWith("mongodb+srv://");
	}

	private static String normalizeMongoUri(String value) {
		var connectionString = new ConnectionString(value);
		if (!isBlank(connectionString.getDatabase())) {
			return value;
		}

		var databaseName = firstNonBlank(System.getenv("CUSTOMER_DB_NAME"), "customer_db");
		var queryIndex = value.indexOf('?');
		var base = queryIndex >= 0 ? value.substring(0, queryIndex) : value;
		var query = queryIndex >= 0 ? value.substring(queryIndex) : "";
		var separator = base.endsWith("/") ? "" : "/";

		return base + separator + databaseName + query;
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
