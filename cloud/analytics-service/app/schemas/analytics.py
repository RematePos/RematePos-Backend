from pydantic import BaseModel


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str


class TopProductItem(BaseModel):
    productId: str | None = None
    productName: str | None = None
    soldQuantity: int
    revenue: float = 0.0


class LowStockProductItem(BaseModel):
    productId: str | None = None
    productName: str | None = None
    currentStock: int
    threshold: int


class SalesTrendItem(BaseModel):
    day: str
    sales: int
    revenue: float


class DashboardSummary(BaseModel):
    totalSales: int
    totalInvoices: int
    totalRevenue: float
    lowStockProducts: int


class BusinessHealth(BaseModel):
    score: int
    level: str
    message: str


class InventoryHealth(BaseModel):
    score: int
    level: str
    criticalProducts: int
    warningProducts: int
    message: str


class SmartRecommendation(BaseModel):
    type: str
    priority: str
    title: str
    message: str
    actionLabel: str
    relatedProductId: str | None = None
    relatedProductName: str | None = None


class StarProduct(BaseModel):
    productId: str | None = None
    productName: str | None = None
    soldUnits7d: int
    revenue7d: float
    message: str


class SlowMovingProduct(BaseModel):
    productId: str | None = None
    productName: str | None = None
    currentStock: int
    soldUnits7d: int
    daysWithoutSales: int | None = None
    message: str


class RestockPredictionItem(BaseModel):
    productId: str | None = None
    productName: str | None = None
    currentStock: int | None = None
    averageDailySales: float | None = None
    estimatedDaysToStockout: float | None = None
    suggestedRestockQuantity: int | None = None
    riskLevel: str | None = None
    recommendationMessage: str | None = None


class DashboardResponse(BaseModel):
    status: str = "CONNECTED"
    source: str = "Backend Analytics"
    dataSourceConfigured: bool = False
    mode: str
    message: str
    summary: DashboardSummary
    businessHealth: BusinessHealth
    inventoryHealth: InventoryHealth
    smartRecommendations: list[SmartRecommendation]
    topProducts: list[TopProductItem]
    lowStockProducts: list[LowStockProductItem]
    salesTrend: list[SalesTrendItem]
    restockPredictions: list[RestockPredictionItem]
    starProducts: list[StarProduct]
    slowMovingProducts: list[SlowMovingProduct]


class TopProductsResponse(BaseModel):
    mode: str
    message: str
    items: list[TopProductItem]


class LowStockProductsResponse(BaseModel):
    mode: str
    message: str
    items: list[LowStockProductItem]


class SalesTrendResponse(BaseModel):
    mode: str
    message: str
    items: list[SalesTrendItem]


class RestockPredictionResponse(BaseModel):
    mode: str
    items: list[RestockPredictionItem]
    message: str
