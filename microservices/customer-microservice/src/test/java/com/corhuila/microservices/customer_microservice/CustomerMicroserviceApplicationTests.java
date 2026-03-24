package com.corhuila.microservices.customer_microservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for Customer Microservice
 * 
 * Run with: mvn test
 * Run single test: mvn test -Dtest=CustomerMicroserviceApplicationTests#testContextLoads
 */
@SpringBootTest
class CustomerMicroserviceApplicationTests {

	@Test
	@DisplayName("Application context should load successfully")
	void contextLoads() {
		// Test passes if context loads without errors
	}


}
