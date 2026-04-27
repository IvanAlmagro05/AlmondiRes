# AlmondiRes

**AlmondiRes** es una aplicación Android diseñada para la gestión integral de reservas de alojamientos. Permite a los usuarios administrar perfiles, visualizar calendarios de disponibilidad, gestionar reservas y consultar estadísticas de ingresos de forma eficiente y multi-idioma.

## 🚀 Características Principales

*   **Gestión de Usuarios:** Registro, inicio de sesión y edición de perfiles.
*   **Gestión de Reservas:** Creación, edición, cancelación y visualización de reservas de alojamientos.
*   **Calendario Interactivo:** Visualización de disponibilidad y fechas ocupadas mediante un calendario dinámico.
*   **Estadísticas:** Análisis de ingresos diarios, mensuales y anuales.
*   **Multi-idioma:** Soporte para Español, Inglés, Francés, Alemán y Chino.
*   **Notificaciones SMS:** Confirmación de reservas mediante el envío de mensajes SMS.
*   **Base de Datos Local:** Almacenamiento persistente mediante SQLite.

## 📁 Estructura del Proyecto (Desglosada)

El proyecto sigue una estructura modular organizada por paquetes dentro de `app/src/main/java/com/example/almondires/`:

### 1. Activities & Fragments (`/activities`)
Contiene la lógica de la interfaz de usuario.
*   `MainActivity.kt`: Actividad principal que gestiona la navegación.
*   `MainActivity_Login.kt`: Gestión del flujo de autenticación inicial.
*   `FragmentLogin.kt` & `FragmentRegister.kt`: Pantallas de acceso y creación de cuentas.
*   `FragmentPerfil.kt` & `FragmentEditarPerfil.kt`: Visualización y edición de datos del usuario y sus alojamientos.
*   `FragmentReservas.kt`: Listado y gestión de las reservas efectuadas.
*   `FragmentCalendario.kt`: Interfaz visual para consultar fechas ocupadas.
*   `FragmentEstadisticas.kt`: Panel visual de ingresos y datos métricos.

### 2. Modelos de Datos (`/models`)
Definen la estructura de los objetos utilizados en la app.
*   `Usuarios.kt`: Atributos del usuario (nombre, email, teléfono, etc.).
*   `Reservas.kt`: Detalles de la reserva (fechas, cliente, precio, noches).
*   `Alojamientos.kt`: Información sobre las propiedades gestionadas.

### 3. Base de Datos (`/database`)
*   `DBHelper.kt`: Clase encargada de la creación de tablas, migración y operaciones CRUD (Create, Read, Update, Delete) sobre la base de datos SQLite.

---

## 🔍 Detalle de Componentes Principales

### 🏠 FragmentPerfil
*   **Actividad Contenedora:** `MainActivity`.
*   **Funcionalidad:** Visualización de datos de usuario, gestión de alojamientos propios (añadir/borrar) y cierre de sesión.
*   **Ciclo de Vida:**
    *   `onViewCreated()`: Inicialización de componentes y carga de datos.
    *   `onResume()`: Refresco automático de la UI al volver de la edición de perfil.
*   **Comunicación:** Envía el `USER_ID` a `FragmentEditarPerfil` vía `Bundle`.
*   **Código:** Configuración de `RecyclerView` con `AlojamientoAdapter`.

### 📅 FragmentReservas
*   **Actividad Contenedora:** `MainActivity`.
*   **Funcionalidad:** 
    1.  **Listado:** Muestra todas las reservas del usuario en un `RecyclerView`.
    2.  **Creación:** Diálogo complejo para seleccionar múltiples alojamientos, fechas (vía `DatePicker`) y cálculo automático de noches/precio.
    3.  **Validación:** Comprobación de fechas libres y campos obligatorios.
    4.  **Confirmación SMS:** Envío automático de mensajes de confirmación al cliente y copia al propietario.
*   **Ciclo de Vida:**
    *   `onViewCreated()`: Recupera el `USER_ID` del intent de la actividad y carga el listado inicial.
*   **Comunicación:** 
    *   Obtiene datos del Intent de `MainActivity`.
    *   Interactúa con `ReservaAdapter` para la lógica de borrado y refresco de la lista.
*   **Código Representativo:**
```kotlin
// Lógica para enviar SMS tras guardar la reserva
private fun enviarConfirmacionSMS(telefono: String, mensaje: String) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(telefono, null, mensaje, null, null)
        Toast.makeText(context, "Confirmación enviada", Toast.LENGTH_SHORT).show()
    } else {
        requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 100)
    }
}
```

---
Desarrollado como solución definitiva para la gestión de alojamientos y reservas.