CREATE OR REPLACE FUNCTION fn_adjust_product_stock(p_product_id INT, p_delta INT)
RETURNS INT
LANGUAGE SQL
AS '
  UPDATE products
  SET stock = stock + p_delta
  WHERE id = p_product_id
  RETURNING stock;
';


