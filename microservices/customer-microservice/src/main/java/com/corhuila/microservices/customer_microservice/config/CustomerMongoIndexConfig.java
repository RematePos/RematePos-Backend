package com.corhuila.microservices.customer_microservice.config;

import com.corhuila.microservices.customer_microservice.customer.model.Customer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class CustomerMongoIndexConfig {

    private static final Logger log = LoggerFactory.getLogger(CustomerMongoIndexConfig.class);

    private final MongoTemplate mongoTemplate;

    public CustomerMongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    void ensureIndexes() {
        try {
            mongoTemplate.indexOps(Customer.class)
                    .ensureIndex(new Index()
                            .named("idx_customer_tenant_id")
                            .on("tenantId", Sort.Direction.ASC));

            mongoTemplate.indexOps(Customer.class)
                    .ensureIndex(new Index()
                            .named("idx_customer_tenant_document")
                            .on("tenantId", Sort.Direction.ASC)
                            .on("documentType", Sort.Direction.ASC)
                            .on("documentNumber", Sort.Direction.ASC));

            mongoTemplate.indexOps(Customer.class)
                    .ensureIndex(new Index()
                            .named("idx_customer_tenant_email")
                            .on("tenantId", Sort.Direction.ASC)
                            .on("email", Sort.Direction.ASC));
        } catch (RuntimeException ex) {
            log.warn("Customer Mongo index creation was deferred: {}", ex.getClass().getSimpleName());
        }
    }
}
