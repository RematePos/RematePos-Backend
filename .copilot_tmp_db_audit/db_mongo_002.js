const cartDb = db.getSiblingDB('cart_db');
cartDb.carts.createIndex(
  { customerId: 1 },
  { name: 'idx_cart_customer_id' }
);
cartDb.carts.createIndex(
  { updatedAt: -1 },
  { name: 'idx_cart_updated_at_desc' }
);

