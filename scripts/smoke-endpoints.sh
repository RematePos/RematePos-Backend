#!/usr/bin/env bash

set -euo pipefail

ENVIRONMENT="${1:-dev}"

# Maps environment name to public ports exposed by compose env files.
case "$ENVIRONMENT" in
  dev)
    CONFIG_PORT=8888
    EUREKA_PORT=8761
    CUSTOMER_PORT=8091
    PRODUCT_PORT=8092
    CART_PORT=8093
    ;;
  qa)
    CONFIG_PORT=18888
    EUREKA_PORT=18761
    CUSTOMER_PORT=18091
    PRODUCT_PORT=18092
    CART_PORT=18093
    ;;
  release)
    CONFIG_PORT=28888
    EUREKA_PORT=28761
    CUSTOMER_PORT=28091
    PRODUCT_PORT=28092
    CART_PORT=28093
    ;;
  main)
    CONFIG_PORT=38888
    EUREKA_PORT=38761
    CUSTOMER_PORT=38091
    PRODUCT_PORT=38092
    CART_PORT=38093
    ;;
  all)
    for env in dev qa release main; do
      "$0" "$env"
    done
    exit 0
    ;;
  *)
    echo "Unknown environment: $ENVIRONMENT"
    echo "Use one of: dev | qa | release | main | all"
    exit 2
    ;;
esac

check_endpoint() {
  local name="$1"
  local url="$2"
  local expected_csv="$3"

  local code
  code="$(curl -s -o /dev/null -w "%{http_code}" "$url" || true)"

  IFS=',' read -r -a expected <<< "$expected_csv"
  for ok in "${expected[@]}"; do
    if [[ "$code" == "$ok" ]]; then
      echo "[OK] $name -> $code ($url)"
      return 0
    fi
  done

  echo "[FAIL] $name -> $code ($url), expected: $expected_csv"
  return 1
}

echo "===== Smoke checks: $ENVIRONMENT ====="
FAILED=0

check_endpoint "config-health" "http://localhost:${CONFIG_PORT}/actuator/health" "200" || FAILED=1
check_endpoint "eureka-health" "http://localhost:${EUREKA_PORT}/actuator/health" "200" || FAILED=1
check_endpoint "customer-list" "http://localhost:${CUSTOMER_PORT}/api/v1/customers" "200" || FAILED=1
check_endpoint "product-list" "http://localhost:${PRODUCT_PORT}/api/v1/products" "200" || FAILED=1
# Cart controller may be pending; accept 404 while service still responds.
check_endpoint "cart-list" "http://localhost:${CART_PORT}/api/v1/carts" "200,404" || FAILED=1

if [[ "$FAILED" -ne 0 ]]; then
  echo "Smoke checks failed for environment: $ENVIRONMENT"
  exit 1
fi

echo "Smoke checks passed for environment: $ENVIRONMENT"
