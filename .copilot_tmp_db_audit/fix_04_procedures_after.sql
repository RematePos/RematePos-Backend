CREATE OR REPLACE PROCEDURE sp_refresh_product_metrics()
LANGUAGE plpgsql
AS '
BEGIN
  IF to_regclass(''mv_category_price_stats'') IS NOT NULL THEN
    EXECUTE ''REFRESH MATERIALIZED VIEW mv_category_price_stats'';
  END IF;
END;
';

