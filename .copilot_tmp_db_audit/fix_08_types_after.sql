DO '
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''product_status'') THEN
    CREATE TYPE product_status AS ENUM (''ACTIVE'', ''INACTIVE'', ''DISCONTINUED'');
  END IF;
END
';

