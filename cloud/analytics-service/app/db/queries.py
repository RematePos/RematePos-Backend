from __future__ import annotations

from decimal import Decimal
from typing import Any

from sqlalchemy import text

from app.db.session import get_engine


def has_operational_data_source() -> bool:
    return any(get_engine(source) is not None for source in ("product", "purchase", "invoice"))


def _fetch_rows(source: str, sql: str, parameters: dict[str, Any]) -> list[dict[str, Any]]:
    engine = get_engine(source)
    if engine is None:
        return []

    with engine.connect() as connection:
        result = connection.execute(text(sql), parameters)
        return [dict(row) for row in result.mappings().all()]


def _as_int(value: Any) -> int:
    if value is None:
        return 0
    return int(value)


def _as_float(value: Any) -> float:
    if value is None:
        return 0.0
    if isinstance(value, Decimal):
        return float(value)
    return float(value)


def get_sales_summary(tenant_id: str) -> dict[str, Any]:
    purchase_rows = _fetch_rows(
        "purchase",
        """
        SELECT
            COUNT(*) AS total_sales,
            COALESCE(SUM(COALESCE(p.total, 0)), 0) AS total_revenue
        FROM purchases p
        WHERE p.tenant_id = :tenant_id
          AND UPPER(COALESCE(p.payment_status, '')) IN ('PAID', 'APPROVED', 'SUCCESS')
        """,
        {"tenant_id": tenant_id},
    )
    invoice_rows = _fetch_rows(
        "invoice",
        """
        SELECT
            COUNT(*) AS total_invoices,
            COALESCE(SUM(COALESCE(i.total, 0)), 0) AS invoice_revenue
        FROM invoices i
        WHERE i.tenant_id = :tenant_id
        """,
        {"tenant_id": tenant_id},
    )

    purchase_summary = purchase_rows[0] if purchase_rows else {}
    invoice_summary = invoice_rows[0] if invoice_rows else {}
    total_sales = _as_int(purchase_summary.get("total_sales"))
    purchase_revenue = _as_float(purchase_summary.get("total_revenue"))
    invoice_revenue = _as_float(invoice_summary.get("invoice_revenue"))

    return {
        "total_sales": total_sales,
        "total_invoices": _as_int(invoice_summary.get("total_invoices")),
        "total_revenue": purchase_revenue if total_sales > 0 else invoice_revenue,
    }


def get_top_products(tenant_id: str, limit: int = 5) -> list[dict[str, Any]]:
    return _fetch_rows(
        "purchase",
        """
        SELECT
            CAST(pi.product_id AS TEXT) AS product_id,
            pi.product_name AS product_name,
            COALESCE(SUM(GREATEST(COALESCE(pi.quantity, 0) - COALESCE(pi.returned_quantity, 0), 0)), 0) AS sold_quantity,
            COALESCE(SUM(COALESCE(pi.line_total, COALESCE(pi.quantity, 0) * COALESCE(pi.unit_price, 0))), 0) AS revenue
        FROM purchase_items pi
        INNER JOIN purchases p ON p.id = pi.purchase_id
        WHERE p.tenant_id = :tenant_id
          AND UPPER(COALESCE(p.payment_status, '')) IN ('PAID', 'APPROVED', 'SUCCESS')
        GROUP BY pi.product_id, pi.product_name
        HAVING COALESCE(SUM(GREATEST(COALESCE(pi.quantity, 0) - COALESCE(pi.returned_quantity, 0), 0)), 0) > 0
        ORDER BY sold_quantity DESC, revenue DESC, pi.product_name ASC
        LIMIT :limit
        """,
        {"tenant_id": tenant_id, "limit": limit},
    )


def get_low_stock_products(tenant_id: str, threshold: int = 5) -> list[dict[str, Any]]:
    return _fetch_rows(
        "product",
        """
        SELECT
            CAST(p.id AS TEXT) AS product_id,
            p.name AS product_name,
            COALESCE(p.stock, 0) AS current_stock,
            COALESCE(p.category_id, NULL) AS category_id
        FROM products p
        WHERE p.tenant_id = :tenant_id
          AND COALESCE(p.stock, 0) <= :threshold
        ORDER BY current_stock ASC, p.name ASC
        """,
        {"tenant_id": tenant_id, "threshold": threshold},
    )


def get_sales_trend(tenant_id: str, days: int = 7) -> list[dict[str, Any]]:
    return _fetch_rows(
        "purchase",
        """
        SELECT
            DATE(COALESCE(p.paid_at, p.created_at)) AS day,
            COUNT(*) AS sales,
            COALESCE(SUM(COALESCE(p.total, 0)), 0) AS revenue
        FROM purchases p
        WHERE p.tenant_id = :tenant_id
          AND UPPER(COALESCE(p.payment_status, '')) IN ('PAID', 'APPROVED', 'SUCCESS')
          AND COALESCE(p.paid_at, p.created_at) >= (CURRENT_DATE - (:days * INTERVAL '1 day'))
        GROUP BY DATE(COALESCE(p.paid_at, p.created_at))
        ORDER BY day ASC
        """,
        {"tenant_id": tenant_id, "days": days},
    )


def get_restock_source_data(tenant_id: str) -> list[dict[str, Any]]:
    product_rows = _fetch_rows(
        "product",
        """
        SELECT
            CAST(p.id AS TEXT) AS product_id,
            p.name AS product_name,
            COALESCE(p.stock, 0) AS current_stock
        FROM products p
        WHERE p.tenant_id = :tenant_id
        ORDER BY p.name ASC
        """,
        {"tenant_id": tenant_id},
    )
    sales_rows = _fetch_rows(
        "purchase",
        """
        SELECT
            CAST(pi.product_id AS TEXT) AS product_id,
            COALESCE(SUM(
                CASE
                    WHEN COALESCE(p.paid_at, p.created_at) >= (CURRENT_DATE - INTERVAL '7 day')
                    THEN GREATEST(COALESCE(pi.quantity, 0) - COALESCE(pi.returned_quantity, 0), 0)
                    ELSE 0
                END
            ), 0) AS sold_units_7d,
            COALESCE(SUM(
                CASE
                    WHEN COALESCE(p.paid_at, p.created_at) >= (CURRENT_DATE - INTERVAL '7 day')
                    THEN COALESCE(pi.line_total, COALESCE(pi.quantity, 0) * COALESCE(pi.unit_price, 0))
                    ELSE 0
                END
            ), 0) AS revenue_7d,
            (CURRENT_DATE - MAX(DATE(COALESCE(p.paid_at, p.created_at)))) AS days_without_sales
        FROM purchase_items pi
        INNER JOIN purchases p ON p.id = pi.purchase_id
        WHERE p.tenant_id = :tenant_id
          AND UPPER(COALESCE(p.payment_status, '')) IN ('PAID', 'APPROVED', 'SUCCESS')
        GROUP BY pi.product_id
        """,
        {"tenant_id": tenant_id},
    )

    sales_by_product = {str(row.get("product_id")): row for row in sales_rows}
    rows: list[dict[str, Any]] = []
    for product in product_rows:
        product_id = str(product.get("product_id"))
        sales = sales_by_product.get(product_id, {})
        rows.append(
            {
                "product_id": product_id,
                "product_name": product.get("product_name"),
                "current_stock": _as_int(product.get("current_stock")),
                "sold_units_7d": _as_int(sales.get("sold_units_7d")),
                "revenue_7d": _as_float(sales.get("revenue_7d")),
                "days_without_sales": sales.get("days_without_sales"),
            }
        )

    return sorted(rows, key=lambda item: (-_as_int(item["sold_units_7d"]), str(item["product_name"] or "")))
