from fastapi import APIRouter, Depends

from app.core.config import get_settings
from app.core.tenant_context import require_tenant_id
from app.schemas.analytics import DashboardResponse, HealthResponse, RestockPredictionResponse
from app.schemas.analytics import LowStockProductsResponse, SalesTrendResponse, TopProductsResponse
from app.services.analytics_service import AnalyticsService
from app.services.prediction_service import PredictionService


router = APIRouter()
settings = get_settings()
analytics_service = AnalyticsService(settings)
prediction_service = PredictionService(settings)


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return analytics_service.health()


@router.get("/dashboard", response_model=DashboardResponse)
def dashboard(tenant_id: str = Depends(require_tenant_id)) -> DashboardResponse:
    return analytics_service.dashboard(tenant_id)


@router.get("/products/top", response_model=TopProductsResponse)
def top_products(tenant_id: str = Depends(require_tenant_id)) -> TopProductsResponse:
    return analytics_service.top_products(tenant_id)


@router.get("/products/low-stock", response_model=LowStockProductsResponse)
def low_stock_products(tenant_id: str = Depends(require_tenant_id)) -> LowStockProductsResponse:
    return analytics_service.low_stock_products(tenant_id)


@router.get("/sales/trend", response_model=SalesTrendResponse)
def sales_trend(tenant_id: str = Depends(require_tenant_id)) -> SalesTrendResponse:
    return analytics_service.sales_trend(tenant_id)


@router.get("/predictions/restock", response_model=RestockPredictionResponse)
def restock_predictions(tenant_id: str = Depends(require_tenant_id)) -> RestockPredictionResponse:
    return prediction_service.restock_predictions(tenant_id)