package com.corhuila.microservices.product_microservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProductMicroserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
