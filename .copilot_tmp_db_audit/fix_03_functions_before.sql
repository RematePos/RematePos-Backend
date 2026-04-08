CREATE OR REPLACE FUNCTION fn_adjust_product_stock(p_product_id INT, p_delta INT)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
  v_new_stock INT;
BEGIN
  UPDATE products
  SET stock = stock + p_delta
  WHERE id = p_product_id
  RETURNING stock INTO v_new_stock;

  IF v_new_stock IS NULL THEN
	RAISE EXCEPTION 'Product % not found', p_product_id;
  END IF;

  IF v_new_stock < 0 THEN
	RAISE EXCEPTION 'Stock cannot be negative for product %', p_product_id;
  END IF;

  RETURN v_new_stock;
END;
$$;

