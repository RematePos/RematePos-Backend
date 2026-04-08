CREATE OR REPLACE FUNCTION fn_touch_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS '
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
';

DROP TRIGGER IF EXISTS trg_products_touch_updated_at ON products;
CREATE TRIGGER trg_products_touch_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION fn_touch_updated_at();

DROP TRIGGER IF EXISTS trg_categories_touch_updated_at ON categories;
CREATE TRIGGER trg_categories_touch_updated_at
BEFORE UPDATE ON categories
FOR EACH ROW
EXECUTE FUNCTION fn_touch_updated_at();

