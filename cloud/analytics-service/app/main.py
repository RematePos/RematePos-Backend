from fastapi import FastAPI

from app.api.routes import router as analytics_router


app = FastAPI(title="RematePOS Analytics", version="0.1.0")
app.include_router(analytics_router, prefix="/api/v1/analytics", tags=["analytics"])