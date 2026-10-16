from sqlalchemy.exc import SQLAlchemyError

from app.core.config import Settings
from app.db import queries
from app.schemas.analytics import RestockPredictionItem, RestockPredictionResponse


class PredictionService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    @staticmethod
    def calculate_risk_level(current_stock: int, estimated_days_to_stockout: float | None, average_daily_sales: float) -> str:
        if current_stock <= 0:
            return "CRITICAL"
        if average_daily_sales == 0:
            return "NO_DATA"
        if estimated_days_to_stockout is not None and estimated_days_to_stockout <= 3:
            return "HIGH"
        if estimated_days_to_stockout is not None and estimated_days_to_stockout <= 7:
            return "MEDIUM"
        return "LOW"

    @staticmethod
    def calculate_suggested_restock_quantity(risk_level: str, average_daily_sales: float) -> int:
        if risk_level in {"HIGH", "CRITICAL"}:
            return max(10, round(average_daily_sales * 7))
        if risk_level == "MEDIUM":
            return round(average_daily_sales * 5)
        return 0

    @staticmethod
    def build_recommendation_message(risk_level: str, suggested_quantity: int) -> str:
        if risk_level == "CRITICAL":
            return f"Producto sin stock. Reponer al menos {suggested_quantity} unidades para recuperar disponibilidad."
        if risk_level == "HIGH":
            return f"Riesgo alto de agotamiento. Reponer al menos {suggested_quantity} unidades esta semana."
        if risk_level == "MEDIUM":
            return f"Monitorear y planear reposicion de {suggested_quantity} unidades pronto."
        if risk_level == "NO_DATA":
            return "No hay ventas recientes suficientes para estimar una reposicion."
        return "Stock estable segun la venta reciente."

    def build_prediction_item(self, row: dict[str, object]) -> RestockPredictionItem:
        current_stock = int(row.get("current_stock") or 0)
        sold_units_7d = float(row.get("sold_units_7d") or 0)
        average_daily_sales = sold_units_7d / 7 if sold_units_7d > 0 else 0.0
        estimated_days = (current_stock / average_daily_sales) if average_daily_sales > 0 else None
        risk_level = self.calculate_risk_level(current_stock, estimated_days, average_daily_sales)
        suggested_quantity = self.calculate_suggested_restock_quantity(risk_level, average_daily_sales)

        return RestockPredictionItem(
            productId=row.get("product_id"),
            productName=row.get("product_name"),
            currentStock=current_stock,
            averageDailySales=round(average_daily_sales, 2),
            estimatedDaysToStockout=round(estimated_days, 2) if estimated_days is not None else None,
            suggestedRestockQuantity=suggested_quantity,
            riskLevel=risk_level,
            recommendationMessage=self.build_recommendation_message(risk_level, suggested_quantity),
        )

    def build_prediction_items(self, rows: list[dict[str, object]]) -> list[RestockPredictionItem]:
        return [self.build_prediction_item(row) for row in rows]

    def restock_predictions(self, tenant_id: str) -> RestockPredictionResponse:
        if not queries.has_operational_data_source():
            return RestockPredictionResponse(mode="DEMO", items=[], message="No data available yet for prediction")

        try:
            rows = queries.get_restock_source_data(tenant_id)
        except SQLAlchemyError:
            return RestockPredictionResponse(mode="NO_DATA", items=[], message="No data available yet for prediction")

        if not rows:
            return RestockPredictionResponse(mode="NO_DATA", items=[], message="No data available yet for prediction")

        items = self.build_prediction_items(rows)
        return RestockPredictionResponse(
            mode="REAL",
            items=items,
            message="Restock predictions loaded from tenant data",
        )
