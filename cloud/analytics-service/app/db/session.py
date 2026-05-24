from __future__ import annotations

from functools import lru_cache

from sqlalchemy import create_engine
from sqlalchemy.engine import Engine
from sqlalchemy.orm import Session, sessionmaker

from app.core.config import get_settings


@lru_cache
def get_database_url(source: str = "default") -> str | None:
    settings = get_settings()
    urls = {
        "default": settings.database_url,
        "product": settings.product_database_url or settings.database_url,
        "purchase": settings.purchase_database_url or settings.database_url,
        "invoice": settings.invoice_database_url or settings.database_url,
    }
    return urls.get(source)


@lru_cache
def get_engine(source: str = "default") -> Engine | None:
    database_url = get_database_url(source)
    if not database_url:
        return None

    return create_engine(database_url, future=True, pool_pre_ping=True)


def get_session(source: str = "default") -> Session | None:
    engine = get_engine(source)
    if engine is None:
        return None

    session_factory = sessionmaker(bind=engine, autoflush=False, autocommit=False, future=True)
    return session_factory()
