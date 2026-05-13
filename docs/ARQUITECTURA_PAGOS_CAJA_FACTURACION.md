# RematePOS - Pagos, caja y facturacion

## Objetivo

RematePOS debe garantizar trazabilidad del pago antes de descontar inventario y antes de emitir factura. La regla principal es:

1. La venta nace en `PENDING_PAYMENT`.
2. El pago se confirma en backend.
3. Solo con pago `APPROVED` se descuenta inventario.
4. Solo despues se solicita la factura/documento POS electronico.
5. Si la factura falla, la venta queda pagada y la factura queda pendiente de emision, no se pierde la venta.

## Arquitectura actual implementada

```text
Frontend React
  -> api-gateway :8080
     -> customer-microservice :8091
     -> product-microservice :8092
     -> cart-microservice :8093
     -> purchase-microservice :8094
     -> invoice-microservice :8095
```

Hoy `purchase-microservice` esta haciendo el papel de `sales-service` y orquestador de pago. El siguiente paso limpio es renombrarlo o separarlo en:

- `sales-service`: venta y detalle.
- `payment-service`: pagos, pasarela, webhooks.
- `cash-register-service`: apertura, movimientos y cierre de caja.
- `billing-service`: DIAN/proveedor tecnologico.

## Flujo textual

```text
Cajero agrega productos
  -> POST /api/v1/purchases/checkout
  -> venta PENDING_PAYMENT
  -> stock no cambia
  -> factura no existe aun

Si pago CASH
  -> POST /api/v1/purchases/{id}/pay
  -> valida efectivo recibido >= total
  -> calcula cambio
  -> registra cash_movements SALE_PAYMENT
  -> descuenta inventario
  -> genera factura
  -> venta INVOICED

Si pago NEQUI/CARD/PSE
  -> POST /api/v1/purchases/{id}/gateway-payment
  -> genera reference y providerTransactionId
  -> pago queda PENDING
  -> webhook sandbox/real confirma APPROVED
  -> valida referencia, monto, moneda, estado y firma
  -> descuenta inventario
  -> genera factura
  -> venta INVOICED
```

## Estados

Venta:

- `PENDING_PAYMENT`
- `PAID`
- `INVOICED`
- `CANCELLED`
- `FAILED`
- `REFUNDED`
- `COMPLETED` y `COMPLETED_WITHOUT_INVOICE` se conservan por compatibilidad con datos anteriores.

Pago:

- `PENDING`
- `APPROVED`
- `DECLINED`
- `FAILED`
- `EXPIRED`
- `REFUNDED`
- `PAID` se conserva por compatibilidad.

Caja:

- Movimiento actual implementado: `SALE_PAYMENT`.
- Tipos preparados: `CASH_IN`, `CASH_OUT`, `SALE_PAYMENT`, `REFUND`, `MANUAL_ADJUSTMENT`.
- Sesiones `OPEN`, `CLOSED`, `CANCELLED` deben vivir en el siguiente `cash-register-service`.

Factura:

- Implementado: factura POS interna con `invoiceNumber`.
- Preparado para evolucionar a `ELECTRONIC_INVOICE` y `POS_ELECTRONIC_DOCUMENT`.

## Endpoints implementados

### Gateway

Base dev:

```text
http://localhost:8080
```

Rutas:

- `/api/v1/customers/**`
- `/api/v1/products/**`
- `/api/v1/categories/**`
- `/api/v1/carts/**`
- `/api/v1/purchases/**`
- `/api/v1/invoices/**`

### Ventas/purchases

Crear venta pendiente:

```http
POST /api/v1/purchases/checkout
```

```json
{
  "documentType": "CC",
  "documentNumber": "1077721349",
  "items": [
    { "productId": 8, "quantity": 1 }
  ],
  "notes": "Venta POS"
}
```

Respuesta esperada:

```json
{
  "purchaseId": 17,
  "status": "PENDING_PAYMENT",
  "paymentStatus": "PENDING",
  "invoiceNumber": null,
  "total": 5950.00
}
```

Pago en efectivo:

```http
POST /api/v1/purchases/{purchaseId}/pay
```

```json
{
  "paymentMethod": "CASH",
  "paidAmount": 5950,
  "cashReceived": 10000,
  "cashRegisterSessionId": 1,
  "evidenceNote": "Pago recibido en caja"
}
```

Respuesta esperada:

```json
{
  "status": "INVOICED",
  "paymentStatus": "APPROVED",
  "paymentMethod": "CASH",
  "cashReceived": 10000.00,
  "changeAmount": 4050.00,
  "invoiceNumber": "INV-20260506-17"
}
```

Crear transaccion electronica sandbox:

```http
POST /api/v1/purchases/{purchaseId}/gateway-payment
```

```json
{
  "paymentMethod": "NEQUI",
  "paymentProvider": "WOMPI",
  "amount": 5950
}
```

Webhook sandbox:

```http
POST /api/v1/purchases/payments/webhook/sandbox
```

```json
{
  "reference": "PAY-16-1778090066924",
  "providerTransactionId": "SANDBOX-...",
  "amount": 5950,
  "currency": "COP",
  "status": "APPROVED",
  "signature": "rematepos-sandbox"
}
```

Buscar por numero de factura desde la compra real:

```http
GET /api/v1/purchases/invoice/INV-20260506-17
```

### Facturas

Buscar copia de factura:

```http
GET /api/v1/invoices/number/INV-20260506-17
```

Tambien se normaliza el error comun de usuario:

```text
NV-20260506-17 -> INV-20260506-17
```

Listar facturas recientes para que el cajero no tenga que adivinar el numero:

```http
GET /api/v1/invoices/recent?limit=8
```

## Datos de prueba

Cliente:

```text
CC 1077721349
carlos villamil
```

Producto usado en pruebas:

```text
productId: 8
nombre: pepeli
```

Facturas generadas durante la prueba:

- `INV-20260506-15`: efectivo, venta queda `INVOICED`.
- `INV-20260506-16`: Nequi sandbox por webhook, venta queda `INVOICED`.
- `INV-20260506-17`: efectivo con movimiento de caja `SALE_PAYMENT`.
- `INV-20260506-18`: efectivo validado por Gateway, stock bajo despues del pago.
- `INV-20260506-19`: Nequi sandbox, stock no bajo hasta webhook `APPROVED`.

## Manejo de errores

- Factura no encontrada responde `404`, no `500`.
- Venta no pendiente responde `400`.
- Efectivo menor al total responde `400`.
- Pago electronico enviado como manual responde `400`.
- Webhook con firma, monto o moneda invalida responde `400`.

## DIAN y proveedor tecnologico

La implementacion actual genera la factura interna. Para Colombia, el camino realista es integrar un proveedor tecnologico autorizado o una API comercial como Dataico, Siigo, Alegra, ePayco/aliados o el proveedor que se elija.

El `billing-service` debe recibir una venta `PAID/INVOICED` y construir un JSON adaptable:

```json
{
  "documentType": "POS_ELECTRONIC_DOCUMENT",
  "saleId": 17,
  "customer": {
    "documentType": "CC",
    "documentNumber": "1077721349",
    "name": "carlos villamil"
  },
  "items": [
    {
      "productId": 8,
      "description": "pepeli",
      "quantity": 1,
      "unitPrice": 5000,
      "taxRate": 0.19
    }
  ],
  "totals": {
    "subtotal": 5000,
    "tax": 950,
    "total": 5950
  }
}
```

La respuesta del proveedor debe guardar:

- `invoiceNumber`
- `cufeOrCude`
- `qrCode`
- `pdfUrl`
- `xmlUrl`
- `status`

Si falla el proveedor, la venta queda pagada y la factura queda `PENDING_ISSUE` o `FAILED`.

## Siguiente separacion recomendada

1. Extraer `purchase-microservice` a `sales-service`.
2. Crear `payment-service` con tabla `payments` y webhook real Wompi/PayU/ePayco.
3. Crear `cash-register-service` con:
   - `cash_register_sessions`
   - `cash_movements`
   - apertura/cierre/resumen.
4. Convertir `invoice-microservice` en `billing-service`.
5. Conectar proveedor DIAN real en modo sandbox.
