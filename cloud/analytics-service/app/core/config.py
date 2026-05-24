from functools import lru_cache

from pydantic import Field
from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    analytics_port: int = Field(default=8090, alias="ANALYTICS_PORT")
    analytics_mode: str = Field(default="DEMO", alias="ANALYTICS_MODE")
    api_gateway_url: str = Field(default="http://localhost:8080", alias="API_GATEWAY_URL")
    database_url: str | None = Field(default=None, alias="DATABASE_URL")
    product_database_url: str | None = Field(default=None, alias="PRODUCT_DATABASE_URL")
    purchase_database_url: str | None = Field(default=None, alias="PURCHASE_DATABASE_URL")
    invoice_database_url: str | None = Field(default=None, alias="INVOICE_DATABASE_URL")
    db_host: str | None = Field(default=None, alias="DB_HOST")
    db_port: int = Field(default=5432, alias="DB_PORT")
    db_name: str | None = Field(default=None, alias="DB_NAME")
    db_user: str | None = Field(default=None, alias="DB_USER")
    db_password: str | None = Field(default=None, alias="DB_PASSWORD")
    jwt_secret: str | None = Field(default=None, alias="JWT_SECRET")
    internal_service_token: str | None = Field(default=None, alias="INTERNAL_SERVICE_TOKEN")

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    @field_validator(
        "database_url",
        "product_database_url",
        "purchase_database_url",
        "invoice_database_url",
        "db_host",
        "db_name",
        "db_user",
        "db_password",
        "jwt_secret",
        "internal_service_token",
        mode="before",
    )
    @classmethod
    def empty_string_to_none(cls, value: str | None) -> str | None:
        if value is None:
            return None
        if isinstance(value, str) and not value.strip():
            return None
        return value


@lru_cache
def get_settings() -> Settings:
    return Settings()
