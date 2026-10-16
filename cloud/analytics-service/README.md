# RematePOS Analytics

RematePOS Analytics is the foundation service for analytics, prediction, and smart business decision support in the RematePOS platform.

No se limita a consultas SQL; transforma datos operativos en recomendaciones accionables para el dueño del negocio.

## Stack

- Python
- FastAPI
- Uvicorn
- Pydantic
- pydantic-settings
- pytest
- httpx

## Install

Create a virtual environment and install dependencies:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

## Run locally

```powershell
python -m uvicorn app.main:app --host 127.0.0.1 --port 8090
```

## Current endpoints

- `GET /api/v1/analytics/health`
- `GET /api/v1/analytics/dashboard`
- `GET /api/v1/analytics/products/top`
- `GET /api/v1/analytics/products/low-stock`
- `GET /api/v1/analytics/sales/trend`
- `GET /api/v1/analytics/predictions/restock`

Tenant-scoped endpoints require the internal `X-Tenant-Id` header. The frontend should not send this header directly; it must be added by the API Gateway or another trusted backend hop after authentication.

## Environment variables

Use `.env.example` as the template.

Do not commit a real `.env` file.

If `DATABASE_URL` is empty, the service starts in safe DEMO/NO_DATA mode and returns controlled responses instead of failing startup.

## Data access

The data-access layer is prepared for tenant-scoped PostgreSQL reads through SQLAlchemy.

Expected source tables for the first Analytics MVP:

- `products`
- `categories`
- `invoices`
- `invoice_items` or an equivalent item-level sales table in the final schema
- `purchases`
- `purchase_items`
- `cash_movements`

If the exact physical names differ, update the SQL adapters in `app/db/queries.py` to match the final backend schema.

## Prediction formula

The service combines three analytics layers:

- Descriptive analytics: shows sales, invoices, products, stock, top products, and sales trend.
- Predictive analytics: estimates days until stockout and stock risk from recent sales.
- Prescriptive analytics: generates restock recommendations, product actions, and business health messages.

Restock prediction uses:

- current stock
- average daily sold units over the last 7 days
- estimated days until stockout
- risk level
- suggested restock quantity

Rules:

- `currentStock <= 0` -> `CRITICAL`
- `estimatedDaysToStockout <= 3` -> `HIGH`
- `estimatedDaysToStockout <= 7` -> `MEDIUM`
- `averageDailySales == 0` -> `NO_DATA`
- otherwise -> `LOW`

Suggested quantity:

- `HIGH` or `CRITICAL` -> `max(10, round(avgDailySales * 7))`
- `MEDIUM` -> `round(avgDailySales * 5)`
- `LOW` or `NO_DATA` -> `0`

## Smart business analytics

`GET /api/v1/analytics/dashboard` returns an enriched decision-support payload:

- `summary`: total sales, invoices, revenue, and low-stock product count.
- `businessHealth`: overall business score, level, and owner-facing message.
- `inventoryHealth`: inventory score, risk counters, and inventory message.
- `smartRecommendations`: prioritized actions such as restock, review slow movers, or protect star products.
- `restockPredictions`: product-level depletion estimate and suggested quantity.
- `starProducts`: products with real recent movement among top products.
- `slowMovingProducts`: products with stock but no sales in the last 7 days.

Inventory health score starts at `100` and subtracts:

- `20` points if there are `CRITICAL` products.
- `10` points if there are `HIGH` products.
- `5` points if there are `MEDIUM` products.
- `5` points if there are several slow-moving products.

The final score is clamped between `0` and `100`.

Business health combines inventory health, recent sales, critical products, and star products. Levels are `EXCELLENT`, `GOOD`, `WARNING`, `CRITICAL`, or `NO_DATA`.

Current limitations:

- This is not heavy machine learning yet.
- It uses simple, explainable rules over tenant-scoped operational data.
- It is a professional foundation for future AI/ML forecasting and recommendation models.

## Roadmap

- Connect to real data sources
- Enforce tenant isolation
- Add restock prediction logic
- Add dashboard metrics frontend support
- Integrate with API gateway
- Prepare deployment packaging
