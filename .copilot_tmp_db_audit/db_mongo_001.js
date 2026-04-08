const customerDb = db.getSiblingDB('customer_db');
customerDb.customers.createIndex(
  { documentType: 1, documentNumber: 1 },
  { unique: true, name: 'uq_customer_document' }
);
customerDb.customers.createIndex(
  { email: 1 },
  { unique: true, sparse: true, name: 'uq_customer_email' }
);

