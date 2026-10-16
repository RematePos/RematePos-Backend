from app.schemas.analytics import (
    BusinessHealth,
    InventoryHealth,
    RestockPredictionItem,
    SalesTrendItem,
    SmartRecommendation,
    StarProduct,
    SlowMovingProduct,
    TopProductItem,
)


class InsightsService:
    @staticmethod
    def calculate_inventory_health(predictions: list[RestockPredictionItem], slow_moving_products: list[SlowMovingProduct]) -> InventoryHealth:
        critical_products = sum(1 for item in predictions if item.riskLevel == "CRITICAL")
        high_products = sum(1 for item in predictions if item.riskLevel == "HIGH")
        medium_products = sum(1 for item in predictions if item.riskLevel == "MEDIUM")
        warning_products = high_products + medium_products

        score = 100
        if critical_products:
            score -= 20
        if high_products:
            score -= 10
        if medium_products:
            score -= 5
        if len(slow_moving_products) >= 3:
            score -= 5
        score = max(0, min(100, score))

        if not predictions:
            return InventoryHealth(
                score=0,
                level="NO_DATA",
                criticalProducts=0,
                warningProducts=0,
                message="No hay datos suficientes para evaluar la salud del inventario.",
            )

        if score >= 90:
            level = "EXCELLENT"
            message = "El inventario se mantiene saludable segun las ventas recientes."
        elif score >= 75:
            level = "GOOD"
            message = "El inventario esta estable, con algunos productos para monitorear."
        elif score >= 50:
            level = "WARNING"
            message = "Hay productos que requieren reposicion o seguimiento pronto."
        else:
            level = "CRITICAL"
            message = "El inventario requiere atencion inmediata por riesgo de agotamiento."

        return InventoryHealth(
            score=score,
            level=level,
            criticalProducts=critical_products,
            warningProducts=warning_products,
            message=message,
        )

    @staticmethod
    def detect_star_products(product_rows: list[dict[str, object]], top_products: list[TopProductItem], limit: int = 3) -> list[StarProduct]:
        top_product_ids = {item.productId for item in top_products if item.soldQuantity > 0 or item.revenue > 0}
        candidates = []
        for row in product_rows:
            sold_units_7d = int(row.get("sold_units_7d") or 0)
            revenue_7d = float(row.get("revenue_7d") or 0.0)
            if row.get("product_id") in top_product_ids and (sold_units_7d > 0 or revenue_7d > 0):
                candidates.append(
                    StarProduct(
                        productId=row.get("product_id"),
                        productName=row.get("product_name"),
                        soldUnits7d=sold_units_7d,
                        revenue7d=revenue_7d,
                        message="Producto con alta rotacion reciente. Mantener disponibilidad y visibilidad.",
                    )
                )

        return sorted(candidates, key=lambda item: (item.soldUnits7d, item.revenue7d), reverse=True)[:limit]

    @staticmethod
    def detect_slow_moving_products(product_rows: list[dict[str, object]], limit: int = 5) -> list[SlowMovingProduct]:
        items = []
        for row in product_rows:
            current_stock = int(row.get("current_stock") or 0)
            sold_units_7d = int(row.get("sold_units_7d") or 0)
            if current_stock > 0 and sold_units_7d == 0:
                days_without_sales = row.get("days_without_sales")
                items.append(
                    SlowMovingProduct(
                        productId=row.get("product_id"),
                        productName=row.get("product_name"),
                        currentStock=current_stock,
                        soldUnits7d=sold_units_7d,
                        daysWithoutSales=int(days_without_sales) if days_without_sales is not None else None,
                        message="Producto con stock disponible sin ventas recientes. Revisar precio, promocion o visibilidad.",
                    )
                )

        return sorted(items, key=lambda item: (item.daysWithoutSales is None, -(item.daysWithoutSales or 0), item.productName or ""))[:limit]

    @staticmethod
    def calculate_business_health(
        inventory_health: InventoryHealth,
        sales_trend: list[SalesTrendItem],
        star_products: list[StarProduct],
        predictions: list[RestockPredictionItem],
    ) -> BusinessHealth:
        if inventory_health.level == "NO_DATA" and not sales_trend and not star_products:
            return BusinessHealth(score=0, level="NO_DATA", message="No hay datos suficientes para evaluar el negocio.")

        score = inventory_health.score
        recent_sales = sum(item.sales for item in sales_trend)
        critical_products = sum(1 for item in predictions if item.riskLevel == "CRITICAL")

        if recent_sales <= 0:
            score -= 15
        if critical_products:
            score -= 10
        if star_products:
            score += 5

        score = max(0, min(100, score))
        if score >= 90:
            level = "EXCELLENT"
            message = "El negocio muestra una operacion saludable y productos con buen movimiento."
        elif score >= 75:
            level = "GOOD"
            message = "El negocio mantiene una operacion estable, pero hay productos para monitorear."
        elif score >= 50:
            level = "WARNING"
            message = "El negocio necesita seguimiento por ventas o inventario en riesgo."
        else:
            level = "CRITICAL"
            message = "El negocio requiere acciones inmediatas sobre inventario y ventas."

        return BusinessHealth(score=score, level=level, message=message)

    @staticmethod
    def generate_smart_recommendations(
        predictions: list[RestockPredictionItem],
        star_products: list[StarProduct],
        slow_moving_products: list[SlowMovingProduct],
        business_health: BusinessHealth,
    ) -> list[SmartRecommendation]:
        recommendations: list[SmartRecommendation] = []

        for item in predictions:
            if item.riskLevel in {"CRITICAL", "HIGH"}:
                recommendations.append(
                    SmartRecommendation(
                        type="RESTOCK",
                        priority=item.riskLevel,
                        title="Reponer producto prioritario",
                        message=item.recommendationMessage or "Reponer este producto para evitar perdida de ventas.",
                        actionLabel="Crear reposicion",
                        relatedProductId=item.productId,
                        relatedProductName=item.productName,
                    )
                )

        for item in slow_moving_products:
            recommendations.append(
                SmartRecommendation(
                    type="SLOW_MOVING",
                    priority="MEDIUM",
                    title="Revisar producto lento",
                    message=item.message,
                    actionLabel="Revisar precio o promocion",
                    relatedProductId=item.productId,
                    relatedProductName=item.productName,
                )
            )

        for item in star_products:
            recommendations.append(
                SmartRecommendation(
                    type="STAR",
                    priority="LOW",
                    title="Proteger producto estrella",
                    message=item.message,
                    actionLabel="Mantener stock",
                    relatedProductId=item.productId,
                    relatedProductName=item.productName,
                )
            )

        if business_health.level in {"CRITICAL", "WARNING"}:
            recommendations.append(
                SmartRecommendation(
                    type="BUSINESS_HEALTH",
                    priority=business_health.level,
                    title="Revisar salud del negocio",
                    message=business_health.message,
                    actionLabel="Revisar dashboard",
                )
            )

        if business_health.level in {"EXCELLENT", "GOOD"} and not any(item.riskLevel in {"CRITICAL", "HIGH"} for item in predictions):
            recommendations.append(
                SmartRecommendation(
                    type="BUSINESS_HEALTH",
                    priority="INFO",
                    title="Operacion estable",
                    message="El negocio mantiene una operacion saludable, sin riesgos criticos detectados.",
                    actionLabel="Mantener seguimiento",
                )
            )

        priority_order = {"CRITICAL": 0, "HIGH": 1, "WARNING": 2, "MEDIUM": 3, "LOW": 4, "INFO": 5}
        return sorted(recommendations, key=lambda item: priority_order.get(item.priority, 5))
