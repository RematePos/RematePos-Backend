# Postman - microservice-pos

Coleccion completa para probar endpoints de `customer`, `product` y `category`.

## Archivos

- `docs/postman/microservice-pos-4-env.postman_collection.json`
- `docs/postman/microservice-pos-dev.postman_environment.json`
- `docs/postman/microservice-pos-qa.postman_environment.json`
- `docs/postman/microservice-pos-release.postman_environment.json`
- `docs/postman/microservice-pos-main.postman_environment.json`

## Uso

1. En Postman, clic en **Import**.
2. Importa la coleccion `microservice-pos-4-env.postman_collection.json`.
3. Importa un environment (dev, qa, release o main).
4. Selecciona el environment en la esquina superior derecha.
5. Ejecuta los endpoints de las carpetas `Customer Service`, `Product Service` y `Category Service`.

## Nota sobre customer

El endpoint base esperado de `customer` es:

- `/api/v1/customers`

Los environments de Postman ya vienen configurados con ese path.
