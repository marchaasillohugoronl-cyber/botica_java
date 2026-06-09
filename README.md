# 💊 Sistema de Gestión de Botica

Sistema web completo para gestión de farmacias y boticas en Perú, desarrollado con Java Spring Boot. Incluye control de productos con fotos, ventas, facturación electrónica integrada con la API de SUNAT (UBL 2.1) y autenticación por roles.

---

## ✨ Funcionalidades

- **Autenticación** — Login con roles `ADMIN` y `VENDEDOR`
- **Productos** — CRUD completo con foto, stock, fecha de vencimiento y alertas de stock bajo
- **Categorías** — Organización de productos por categoría
- **Ventas** — Punto de venta con búsqueda de productos en tiempo real y carrito
- **Facturación electrónica** — Generación de Boletas y Facturas en formato UBL 2.1 para SUNAT
- **Dashboard** — Resumen de ventas del día, alertas de stock y productos por vencer
- **Comprobantes** — Vista de impresión para boletas y facturas

---

## 🛠 Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3.2 · Spring Security · Spring Data JPA |
| Base de datos | PostgreSQL (Neon) · H2 (desarrollo local) |
| Frontend | Thymeleaf · Bootstrap 5 · Bootstrap Icons |
| Build | Maven 3.9 |
| Deploy | Render (Docker) · Neon (PostgreSQL serverless) |

---

## 🚀 Ejecutar en local (desarrollo)

**Requisito:** Java 17 instalado → [adoptium.net](https://adoptium.net/)

```bash
# Clonar el repositorio
git clone https://github.com/TU_USUARIO/botica-sistema.git
cd botica-sistema

# Compilar y ejecutar (descarga Maven automáticamente)
./mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run              # Linux / Mac
```

La aplicación inicia en **http://localhost:8080**

> Usa base de datos H2 embebida en modo desarrollo. Los datos se guardan en `./data/boticadb`.  
> Consola H2 disponible en: **http://localhost:8080/h2-console**

### Usuarios iniciales

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | Administrador (acceso total) |
| `vendedor` | `vendedor123` | Vendedor (ventas y consultas) |

---

## ☁️ Despliegue en Render + Neon

### 1 — Crear base de datos en Neon

1. Registro gratuito en [neon.tech](https://neon.tech)
2. Crear proyecto → copiar el **Connection string**
3. Agregar prefijo `jdbc:` al inicio:
   ```
   jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```

### 2 — Crear cuenta en Cloudinary (fotos de productos)

1. Registro gratuito en [cloudinary.com](https://cloudinary.com) (25 GB gratis)
2. En el panel ir a **Settings → API Keys**
3. Copiar la **API Environment variable**, se ve así:
   ```
   cloudinary://234567890123456:AbCdEfGhIjKlMnOpQrStUvWxYz@tu-cloud-name
   ```
4. Guardar ese valor — se usará como variable `CLOUDINARY_URL` en Render

> En **desarrollo local** no se necesita Cloudinary. Las fotos se guardan en `./uploads/productos/`.

### 3 — Subir código a GitHub

```bash
git init
git add .
git commit -m "Sistema Botica v1.0"
git remote add origin https://github.com/TU_USUARIO/botica-sistema.git
git push -u origin main
```

### 3 — Crear Web Service en Render

1. [render.com](https://render.com) → **New → Web Service**
2. Conectar repositorio GitHub
3. Render detecta el `Dockerfile` automáticamente

### 4 — Variables de entorno en Render

| Variable | Descripción |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL de Neon (con `jdbc:` al inicio) |
| `SPRING_DATASOURCE_USERNAME` | Usuario de Neon |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de Neon |
| `CLOUDINARY_URL` | URL de Cloudinary (ver paso 3b) |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SUNAT_RUC` | RUC del emisor (11 dígitos) |
| `SUNAT_RAZON_SOCIAL` | Razón social registrada en SUNAT |
| `SUNAT_USUARIO_SOL` | Usuario SOL de SUNAT |
| `SUNAT_CLAVE_SOL` | Clave SOL de SUNAT |
| `SUNAT_MODO` | `beta` (pruebas) o `produccion` |

### 5 — Deploy

Render construye la imagen Docker y despliega en:
```
https://botica-sistema.onrender.com
```

---

## 🧾 Integración SUNAT

El sistema genera comprobantes en formato **UBL 2.1** conforme a las especificaciones de SUNAT.

| Tipo | Serie | Descripción |
|---|---|---|
| Boleta de Venta | `B001` | Para consumidores finales (DNI) |
| Factura Electrónica | `F001` | Para empresas (RUC) |

### Flujo de emisión

```
Venta → Generar XML UBL 2.1 → Firmar con certificado X.509
     → Comprimir ZIP → Enviar SOAP a SUNAT → Recibir CDR
```

### Pasar a producción SUNAT

1. **Obtener certificado digital X.509** de una entidad certificadora autorizada por SUNAT
2. Implementar la firma en `ServicioSunat.java` → método `firmarXml()`
3. Cambiar `SUNAT_MODO=produccion` en las variables de entorno
4. Registrar el RUC como **Emisor Electrónico** en el portal SUNAT

> En modo `beta` el sistema envía a los servidores de prueba de SUNAT y simula la respuesta CDR sin necesidad de certificado.

---

## 📁 Estructura del proyecto

```
src/main/java/com/botica/
├── config/          ConfiguracionSeguridad, ConfiguracionWeb, InicializadorDatos
├── controller/      ControladorPanel, ControladorProductos, ControladorVentas,
│                    ControladorCategorias, ControladorAutenticacion
├── model/           Producto, Categoria, Venta, DetalleVenta, Comprobante,
│                    Usuario, Rol
├── repository/      ProductoRepositorio, VentaRepositorio, ComprobanteRepositorio,
│                    CategoriaRepositorio, UsuarioRepositorio
├── service/         ServicioProductos, ServicioVentas, ServicioSunat,
│                    ServicioCategorias, ServicioAlmacenamiento, ServicioDetallesUsuario
└── dto/             VentaDTO, DetalleVentaDTO

src/main/resources/
├── templates/       Vistas Thymeleaf (dashboard, productos, ventas, categorías, login)
├── static/          CSS y JS personalizados
├── application.properties              Configuración desarrollo (H2)
└── application-prod.properties         Configuración producción (PostgreSQL/Neon)
```

---

## ⚠️ Notas de producción

- **Imágenes de productos:** Render tier gratuito no tiene disco persistente. Las fotos se perderán al reiniciar. Para producción real integrar Cloudflare R2 o AWS S3.
- **Sesiones:** Render puede pausar el servicio gratuito tras 15 min de inactividad (cold start ~30 seg).
- **Base de datos Neon:** El tier gratuito tiene límite de 5 GB de almacenamiento y 100 conexiones concurrentes (pooled).

---

## 📄 Licencia

MIT License — libre uso para proyectos personales y comerciales.
