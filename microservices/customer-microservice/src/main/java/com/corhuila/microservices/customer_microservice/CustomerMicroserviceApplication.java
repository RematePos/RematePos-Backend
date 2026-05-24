package com.corhuila.microservices.customer_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerMicroserviceApplication {

	public static void main(String[] args) {
		applyMongoUriEnvironmentFallback();
		SpringApplication.run(CustomerMicroserviceApplication.class, args);
	}

	private static void applyMongoUriEnvironmentFallback() {
		var explicitMongoUri = firstNonBlank(
				System.getenv("SPRING_DATA_MONGODB_URI"),
				System.getenv("SPRING_MONGODB_URI")
		);

		if (explicitMongoUri != null && isBlank(System.getProperty("spring.data.mongodb.uri"))) {
			System.setProperty("spring.data.mongodb.uri", explicitMongoUri);
		}
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (!isBlank(value)) {
				return value;
			}
		}
		return null;
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
