from sqlalchemy.exc import SQLAlchemyError

from app.core.config import Settings
from app.db import queries
from app.schemas.analytics import (
    DashboardResponse,
    DashboardSummary,
    HealthResponse,
    InventoryHealth,
    LowStockProductItem,
    LowStockProductsResponse,
    SalesTrendItem,
    SalesTrendResponse,
    SmartRecommendation,
    TopProductItem,
    TopProductsResponse,
)
from app.services.insights_service import InsightsService
from app.services.prediction_service import PredictionService


class AnalyticsService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._prediction_service = PredictionService(settings)
        self._insights_service = InsightsService()

    def health(self) -> HealthResponse:
        return HealthResponse(status="UP", service="rematepos-analytics", version="0.1.0")

    def dashboard(self, tenant_id: str) -> DashboardResponse:
        if not queries.has_operational_data_source():
            return self._empty_dashboard(mode="DEMO", message="Analytics connected in demo mode; no data source configured yet")

        try:
            summary = queries.get_sales_summary(tenant_id)
            top_products = [
                TopProductItem(
                    productId=item.get("product_id"),
                    productName=item.get("product_name"),
                    soldQuantity=int(item.get("sold_quantity", 0) or 0),
                    revenue=float(item.get("revenue", 0.0) or 0.0),
                )
                for item in queries.get_top_products(tenant_id)
            ]
            low_stock_rows = queries.get_low_stock_products(tenant_id)
            low_stock_products = [
                LowStockProductItem(
                    productId=row.get("product_id"),
                    productName=row.get("product_name"),
                    currentStock=int(row.get("current_stock", 0) or 0),
                    threshold=5,
                )
                for row in low_stock_rows
            ]
            sales_trend = [
                SalesTrendItem(day=str(item.get("day")), sales=int(item.get("sales", 0) or 0), revenue=float(item.get("revenue", 0.0) or 0.0))
                for item in queries.get_sales_trend(tenant_id)
            ]
            restock_source_rows = queries.get_restock_source_data(tenant_id)
        except SQLAlchemyError:
            return self._empty_dashboard(mode="NO_DATA", message="No analytics data available yet for this tenant")

        restock_predictions = self._prediction_service.build_prediction_items(restock_source_rows)
        star_products = self._insights_service.detect_star_products(restock_source_rows, top_products)
        slow_moving_products = self._insights_service.detect_slow_moving_products(restock_source_rows)
        inventory_health = self._insights_service.calculate_inventory_health(restock_predictions, slow_moving_products)
        business_health = self._insights_service.calculate_business_health(inventory_health, sales_trend, star_products, restock_predictions)
        smart_recommendations = self._insights_service.generate_smart_recommendations(
            restock_predictions,
            star_products,
            slow_moving_products,
            business_health,
        )
        has_sales = any(
            [
                int(summary.get("total_sales", 0) or 0) > 0,
                int(summary.get("total_invoices", 0) or 0) > 0,
                float(summary.get("total_revenue", 0.0) or 0.0) > 0,
                bool(top_products),
                bool(sales_trend),
            ]
        )
        baseline_recommendations = self._baseline_recommendations(
            has_products=bool(restock_source_rows),
            has_sales=has_sales,
            has_low_stock=bool(low_stock_products),
        )
        if not smart_recommendations:
            smart_recommendations = baseline_recommendations
        elif not has_sales:
            smart_recommendations = [*smart_recommendations, *baseline_recommendations]

        has_data = any(
            [
                has_sales,
                bool(top_products),
                bool(low_stock_products),
                bool(sales_trend),
                bool(restock_predictions),
                bool(restock_source_rows),
            ]
        )
        mode = "REAL" if has_data else "NO_DATA"

        return DashboardResponse(
            dataSourceConfigured=True,
            mode=mode,
            message="Analytics metrics loaded from tenant data" if mode == "REAL" else "Connected to tenant data; no operational records yet",
            summary=DashboardSummary(
                totalSales=int(summary.get("total_sales", 0) or 0),
                totalInvoices=int(summary.get("total_invoices", 0) or 0),
                totalRevenue=float(summary.get("total_revenue", 0.0) or 0.0),
                lowStockProducts=len(low_stock_rows),
            ),
            businessHealth=business_health,
            inventoryHealth=inventory_health,
            smartRecommendations=smart_recommendations,
            topProducts=top_products,
            lowStockProducts=low_stock_products,
            salesTrend=sales_trend,
            restockPredictions=restock_predictions,
            starProducts=star_products,
            slowMovingProducts=slow_moving_products,
        )

    def _empty_dashboard(self, mode: str, message: str) -> DashboardResponse:
        inventory_health = InventoryHealth(
            score=0,
            level="NO_DATA",
            criticalProducts=0,
            warningProducts=0,
            message="No hay datos suficientes para evaluar la salud del inventario.",
        )
        business_health = self._insights_service.calculate_business_health(inventory_health, [], [], [])

        return DashboardResponse(
            dataSourceConfigured=False,
            mode=mode,
            message=message,
            summary=DashboardSummary(totalSales=0, totalInvoices=0, totalRevenue=0.0, lowStockProducts=0),
            businessHealth=business_health,
            inventoryHealth=inventory_health,
            smartRecommendations=self._baseline_recommendations(has_products=False, has_sales=False, has_low_stock=False),
            topProducts=[],
            lowStockProducts=[],
            salesTrend=[],
            restockPredictions=[],
            starProducts=[],
            slowMovingProducts=[],
        )

    @staticmethod
    def _baseline_recommendations(
        has_products: bool,
        has_sales: bool,
        has_low_stock: bool,
    ) -> list[SmartRecommendation]:
        recommendations: list[SmartRecommendation] = []

        if not has_sales:
            recommendations.append(
                SmartRecommendation(
                    type="SALES_HISTORY",
                    priority="INFO",
                    title="Activar historial de ventas",
                    message="Registra ventas para activar prediccion de demanda.",
                    actionLabel="Registrar ventas",
                )
            )
            recommendations.append(
                SmartRecommendation(
                    type="FORECAST",
                    priority="INFO",
                    title="Prediccion en preparacion",
                    message="Cuando existan ventas historicas, el sistema sugerira reposicion automatica.",
                    actionLabel="Mantener seguimiento",
                )
            )

        if has_products:
            recommendations.append(
                SmartRecommendation(
                    type="INVENTORY_DATA",
                    priority="INFO",
                    title="Inventario conectado",
                    message="Manten actualizado el inventario para mejorar las recomendaciones.",
                    actionLabel="Revisar inventario",
                )
            )
        else:
            recommendations.append(
                SmartRecommendation(
                    type="PRODUCT_CATALOG",
                    priority="MEDIUM",
                    title="Catalogo sin productos",
                    message="Agrega productos del negocio para habilitar analitica de inventario.",
                    actionLabel="Crear producto",
                )
            )

        if has_low_stock:
            recommendations.append(
                SmartRecommendation(
                    type="RESTOCK",
                    priority="HIGH",
                    title="Revisar bajo stock",
                    message="Revisa productos con bajo stock para evitar faltantes durante la venta.",
                    actionLabel="Reponer inventario",
                )
            )

        return recommendations

    def top_products(self, tenant_id: str, limit: int = 5) -> TopProductsResponse:
        if not queries.has_operational_data_source():
            return TopProductsResponse(mode="DEMO", message="Analytics data source not configured yet", items=[])

        items = [
            TopProductItem(
                productId=item.get("product_id"),
                productName=item.get("product_name"),
                soldQuantity=int(item.get("sold_quantity", 0) or 0),
                revenue=float(item.get("revenue", 0.0) or 0.0),
            )
            for item in queries.get_top_products(tenant_id, limit=limit)
        ]
        mode = "REAL" if items else "NO_DATA"
        return TopProductsResponse(
            mode=mode,
            message="Tenant top products loaded" if items else "No analytics data available yet for this tenant",
            items=items,
        )

    def low_stock_products(self, tenant_id: str, threshold: int = 5) -> LowStockProductsResponse:
        if not queries.has_operational_data_source():
            return LowStockProductsResponse(mode="DEMO", message="Analytics data source not configured yet", items=[])

        rows = queries.get_low_stock_products(tenant_id, threshold=threshold)
        items = [
            LowStockProductItem(
                productId=row.get("product_id"),
                productName=row.get("product_name"),
                currentStock=int(row.get("current_stock", 0) or 0),
                threshold=threshold,
            )
            for row in rows
        ]
        mode = "REAL" if items else "NO_DATA"
        return LowStockProductsResponse(
            mode=mode,
            message="Tenant low stock products loaded" if items else "No analytics data available yet for this tenant",
            items=items,
        )

    def sales_trend(self, tenant_id: str, days: int = 7) -> SalesTrendResponse:
        if not queries.has_operational_data_source():
            return SalesTrendResponse(mode="DEMO", message="Analytics data source not configured yet", items=[])

        items = [
            SalesTrendItem(day=str(item.get("day")), sales=int(item.get("sales", 0) or 0), revenue=float(item.get("revenue", 0.0) or 0.0))
            for item in queries.get_sales_trend(tenant_id, days=days)
        ]
        mode = "REAL" if items else "NO_DATA"
        return SalesTrendResponse(
            mode=mode,
            message="Tenant sales trend loaded" if items else "No analytics data available yet for this tenant",
            items=items,
        )
