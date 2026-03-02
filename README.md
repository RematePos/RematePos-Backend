⚙️ RematePos-Backend

Servicios Distribuidos para el Sistema rematePOS

📖 Descripción

RematePos-Backend es el repositorio encargado de la lógica de negocio y los servicios fundamentales para el funcionamiento del sistema rematePOS, una plataforma distribuida de punto de venta orientada a cacharrerías y locales de remates.

Desarrollado con Java y Spring Boot, el backend gestiona la interacción entre los usuarios, la base de datos y los microservicios, asegurando un procesamiento eficiente, seguro y escalable de las operaciones comerciales. Este sistema proporciona una arquitectura desacoplada, diseñada para soportar el crecimiento y la integración de nuevos módulos y servicios.

🏗️ Arquitectura y Tecnologías

El backend está basado en una arquitectura de microservicios que garantiza flexibilidad, escalabilidad y alta disponibilidad. Algunas de las tecnologías clave utilizadas son:

🛠️ Java + Spring Boot: Framework robusto y ampliamente utilizado para la creación de aplicaciones empresariales y microservicios.

🗄️ Spring Data: Facilita la interacción con bases de datos relacionales y no relacionales.

🔐 Spring Security: Para una gestión avanzada de la autenticación y autorización de usuarios.

🔄 API REST: Servicios RESTful para interactuar con el frontend y otros servicios del ecosistema.

📊 JPA/Hibernate: Mapeo objeto-relacional para facilitar la persistencia de datos en la base de datos.

📈 Swagger/OpenAPI: Documentación interactiva para facilitar el consumo y la integración con otros servicios.

📦 Funcionalidades Principales

El backend gestiona las siguientes funciones clave del sistema:

📦 Gestión de Inventarios: Actualización y seguimiento en tiempo real de los productos disponibles en los locales y bodegas.

💳 Registro de Ventas: Creación y validación de transacciones de venta, integrando múltiples métodos de pago.

🧾 Facturación Electrónica: Generación y envío de facturas electrónicas, cumpliendo con las normativas de la DIAN.

🔗 Integración de Microservicios: Comunicación entre servicios desacoplados para facilitar la escalabilidad y la resiliencia del sistema.

🧑‍💻 Gestión de Usuarios y Roles: Control de acceso y gestión de permisos según los distintos tipos de usuarios (administradores, cajeros, etc.).

🔄 Integración con el Ecosistema rematePOS

RematePos-Backend interactúa con los siguientes componentes clave del sistema:

💻 Frontend: Consumo de los servicios backend mediante solicitudes API para la visualización y manipulación de datos.

🗄️ Base de Datos: Almacenamiento y recuperación de información relacionada con inventarios, ventas y clientes.

🔌 API Gateway: Orquestación de las solicitudes entre los distintos microservicios, asegurando una experiencia fluida para el usuario final.

🚀 Proyección

El backend de rematePOS está diseñado con una visión de crecimiento y expansión. A medida que el sistema evoluciona, el backend se adaptará para soportar:

🌍 Escalabilidad Multisucursal: Soporte para múltiples puntos de venta y locales.

💡 Optimización para entornos productivos: Preparado para la migración a la nube y alta disponibilidad.

🔐 Cumplimiento de normativas: Integración con proveedores autorizados por la DIAN para facturación electrónica.

👥 Equipo de Desarrollo

Product Owner (PO): Kevin Santiago Cuesta Hernández

Backend: Carlos Andrés Villamil Yusunguaira

Frontend: Andrés Felipe Ardila Fajardo

QA: Juan Sebastián Murcia Vargas

📌 Estado del Proyecto

🚧 En desarrollo — Proyecto académico para la asignatura Sistemas Distribuidos, con una visión comercial para su evolución hacia un sistema de gestión de ventas profesional y totalmente funcional.
