# HU-178D - Tenant User Management

## 1. Problema

HU-178C permite que un platform admin cree un tenant y su business owner inicial, pero el negocio todavia no tiene endpoints backend para que el owner o admin gestione cajeros y administradores internos del tenant.

## 2. Objetivo

Agregar administracion backend de usuarios por tenant sin exponer passwords, hashes, tokens ni secretos, y manteniendo el aislamiento entre tenants.

## 3. Endpoints agregados

- `POST /api/v1/tenants/{tenantId}/users`: crea un usuario dentro del tenant.
- `GET /api/v1/tenants/{tenantId}/users`: lista usuarios del tenant.
- `PATCH /api/v1/tenants/{tenantId}/users/{userId}`: actualiza email, fullName, active y rol permitido.
- `PATCH /api/v1/tenants/{tenantId}/users/{userId}/disable`: desactiva la membership del usuario en el tenant.
- `PATCH /api/v1/tenants/{tenantId}/users/{userId}/enable`: reactiva la membership del usuario en el tenant.

## 4. Roles permitidos

Desde los endpoints de tenant users solo se pueden asignar:

- `CASHIER`
- `BUSINESS_ADMIN`

No se permite crear ni asignar `PLATFORM_SUPER_ADMIN` desde endpoints tenant. `BUSINESS_OWNER` queda reservado para onboarding o administracion de plataforma. `QA_SUPPORT` existe como rol de plataforma, por lo que no se asigna como rol tenant.

## 5. Seguridad

- Sin token, el gateway/auth devuelve `401`.
- `CASHIER` no tiene `USERS_CREATE` ni `USERS_UPDATE`, por lo que no puede administrar usuarios.
- `BUSINESS_OWNER` y `BUSINESS_ADMIN` pueden administrar usuarios si tienen permisos tenant.
- Un usuario de otro tenant recibe `403`.
- `PLATFORM_SUPER_ADMIN` puede administrar usuarios de cualquier tenant.
- No se confia en tenantId del body; solo se usa el tenantId de la URL y el usuario autenticado.
- Passwords se almacenan con BCrypt.
- Las respuestas no incluyen password, passwordHash, secretos ni tokens.
- La desactivacion no borra usuarios ni datos historicos; desactiva la membership del tenant.
- No se permite desactivar al unico `BUSINESS_OWNER` activo del tenant.

## 6. Flujo

Owner login -> create cashier -> cashier login -> cashier has limited permissions.

El cashier creado recibe permisos del rol `CASHIER`, por lo que puede leer productos/categorias, crear clientes y operar ventas segun los permisos existentes, pero no puede crear productos, crear categorias ni crear usuarios.

## 7. Pruebas

Smoke backend ejecutado:

- Gateway health retorno `200`.
- Login platform admin retorno `200`.
- Platform admin creo tenant y owner con `201`.
- Owner login con tenant activo retorno `200`.
- Crear usuario tenant sin token retorno `401`.
- Owner creo cashier en su tenant con `201`.
- Cashier login retorno `200`.
- Owner creo categoria y producto con `200`.
- Cashier pudo leer productos con `200`.
- Cashier no pudo crear producto, categoria ni usuarios; retorno `403`.
- Owner listo usuarios del tenant con `200`.
- Cashier creo cliente con `200`.
- Cashier ejecuto checkout con `200`.
- Owner desactivo cashier con `200`.
- Cashier desactivado no pudo volver a iniciar sesion; retorno `401`.
- Owner de tenant A no pudo listar usuarios de tenant B; retorno `403`.
- Platform admin sin tenant no pudo operar productos; retorno `403`.

## 8. Riesgos/deudas

- No existe permiso `USERS_READ`; temporalmente el listado acepta `USERS_CREATE` o `USERS_UPDATE`.
- La invalidacion inmediata de JWT ya emitidos no existe en el stack actual; una membership desactivada impide nuevos logins tenant, pero tokens emitidos previamente expiran segun la configuracion JWT.
- No se agrego migracion porque las tablas actuales ya soportan usuarios, roles y memberships activas.

## 9. Pendientes

- UI frontend para administracion de usuarios.
- Recuperacion/cambio de contrasena.
- Auditoria de cambios de usuarios.
- Invitaciones por email.
- Implement token revocation / session invalidation to immediately block already-issued JWTs after disabling a tenant user.
- Evaluate short-lived access tokens and refresh token strategy for production environments.
