from fastapi import Header, HTTPException, status


def require_tenant_id(x_tenant_id: str | None = Header(default=None, alias="X-Tenant-Id")) -> str:
    if x_tenant_id is None or not x_tenant_id.strip():
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="X-Tenant-Id header is required for tenant-scoped analytics endpoints",
        )

    return x_tenant_id.strip()
