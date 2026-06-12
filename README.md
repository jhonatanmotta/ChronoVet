# 🐾 ChronoVet - Sistema de Gestión Veterinaria

![Java Version](https://img.shields.io/badge/Java-17-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Swing](https://img.shields.io/badge/Swing-GUI-orange)
![NetBeans](https://img.shields.io/badge/NetBeans-18-1B6AC6)

> Sistema de agendamiento de citas para veterinarias desarrollado en Java SE con Swing

----

## 📋 Tabla de Contenidos
- [Acerca del Proyecto](#acerca-del-proyecto)
- [Características](#características)
- [Tecnologías](#tecnologías)
- [Instalación](#instalación)
- [Estructura de la Base de Datos](#estructura-de-la-base-de-datos)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Funcionalidades Implementadas](#funcionalidades-implementadas)
- [Reglas de Negocio](#reglas-de-negocio)
- [Capturas de Pantalla](#capturas-de-pantalla)
- [Licencia](#licencia)

---

## 🎯 Acerca del Proyecto

**ChronoVet** es un sistema integral para la gestión de citas veterinarias que permite:

- 📅 Agendamiento automático de citas
- 👨‍⚕️ Asignación inteligente de veterinarios por especialidad
- 📊 Dashboards personalizados por rol
- 🔐 Autenticación segura con encriptación de contraseñas
- 📝 Historial médico de mascotas

### 👨‍🏫 Información Académica

| Campo | Detalle |
|-------|---------|
| **Asignatura** | Tecnología de Desarrollo de Sistemas Informáticos |
| **Semestre** | III Semestre - 1-2026 |
| **Profesor** | Mag. Carlos Adolfo Beltrán Castro |

### 👨‍💻 Equipo de Desarrollo

| Estudiante | Código |
|------------|--------|
| Jhonatan David Motta Medina | 1098612199 |
| Nilmer Giovanny Osorio Rueda | 1096067828 |
| Juan Jose Ortiz Rosales | 1005288682 |

---

## ✨ Características

### 👥 Roles del Sistema

| Rol | Funcionalidades |
|-----|-----------------|
| **Administrador** | Gestión completa de usuarios, dashboards globales |
| **Veterinario** | Registrar consultas, ver citas asignadas, historial |
| **Administrativo** | Gestión de citas, dashboard operativo |
| **Cliente** | Agendar citas, ver historial de mascotas |

---

## 🛠️ Tecnologías

### Backend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java SE | 17 | Lógica de negocio |
| PostgreSQL | 16 | Base de datos |
| JDBC | - | Conexión a BD |

### Frontend
| Tecnología | Uso |
|------------|-----|
| Swing | Interfaz gráfica |
| FlatLaf | Tema visual moderno |
| CardLayout | Navegación entre paneles |

### Herramientas
- **IDE**: Apache NetBeans 18
- **Gestor BD**: pgAdmin
- **Control de versiones**: Git & GitHub

---

## 🔧 Instalación

### Prerrequisitos

```bash
# Java 17 o superior
java --version

# PostgreSQL 16 o superior
psql --version
```

### 1. Clonar el repositorio

```bash
git clone https://github.com/jhonatanmotta/ChronoVet.git
cd ChronoVet
```

### 2. Configurar la base de datos

```sql
-- Crear la base de datos
CREATE DATABASE veterinaria_db;

-- Conectarse a la base
\c veterinaria_db;

-- Ejecutar el script de creación
\i db/ChronoVet.sql
```

### 3. Configurar la conexión

Editar el archivo:

```text
src/conexion/Conexion.java
```

y actualizar los datos de conexión:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/veterinaria_db";
private static final String USER = "tu_usuario";
private static final String PASSWORD = "tu_contraseña";
```

### 4. Agregar dependencias

Verificar que el proyecto tenga configurados los siguientes JAR en el classpath:

- PostgreSQL JDBC Driver (`postgresql-42.x.x.jar`)
- FlatLaf (`flatlaf-3.x.jar`)
- FlatLaf Extras (`flatlaf-extras-3.x.jar`)
- JCalendar (`jcalendar-x.x.jar`)

### 5. Ejecutar la aplicación

Iniciar la aplicación desde la clase principal:

```text
src/main/Main.java
```

---

## 📊 Estructura de la Base de Datos

### Diagrama Entidad-Relación

![Diagrama ER](src/documentacion/Diagrama%20ER.png)

> `src/documentacion/Diagrama ER.png`

> **📌 Nota:** La imagen del diagrama entidad-relación mostrada arriba se encuentra desactualizada y no incluye todas las tablas implementadas en el sistema. Para ver la estructura completa y actualizada de la base de datos, consulte el archivo `db/veterinaria_db.sql` que contiene el script completo con todas las tablas, relaciones, restricciones y procedimientos almacenados del sistema.

### Modelo de Datos

| Tabla | Descripción |
|---------|-------------|
| `usuarios` | Datos de usuarios y autenticación |
| `roles` | Roles del sistema |
| `veterinarios` | Información profesional de los veterinarios |
| `especialidades` | Especialidades médicas veterinarias |
| `mascotas` | Mascotas registradas por los clientes |
| `citas` | Agendamiento y control de citas |
| `consultas` | Historial médico de las mascotas |
| `ausencias_veterinario` | Registro de horarios no disponibles de los veterinarios |
| `auditoria_citas` | Historial y seguimiento de cambios realizados sobre las citas |

## 🔐 Seguridad y Encriptación

Las contraseñas de los usuarios no se almacenan en texto plano.

El sistema utiliza la función `crypt()` de PostgreSQL junto con el algoritmo **Blowfish (bf)** para garantizar la seguridad de las credenciales.

---

## 🗂️ Estructura del Proyecto

```text
ChronoVet/
├── src/
│   ├── main/
│   │   └── Main.java
│   ├── controlador/
│   │   ├── Ctrl_login.java
│   │   ├── Ctrl_menu.java
│   │   ├── Ctrl_Dashboard.java
│   │   ├── Ctrl_DashboardAdministrador.java
│   │   ├── Ctrl_DashboardAdministrativo.java
│   │   ├── Ctrl_DashboardCliente.java
│   │   ├── Ctrl_DashboardVeterinario.java
│   │   ├── Ctrl_Cita.java
│   │   ├── Ctrl_Consulta.java
│   │   ├── Ctrl_GestionCitas.java
│   │   ├── Ctrl_Mascota.java
│   │   ├── Ctrl_usuario.java
│   │   └── Ctrl_gestionUsuarios.java
│   ├── modelo/
│   │   ├── Usuario.java
│   │   ├── UsuarioDAO.java
│   │   ├── Cita.java
│   │   ├── CitaDAO.java
│   │   ├── Consulta.java
│   │   ├── ConsultaDAO.java
│   │   ├── Mascota.java
│   │   ├── MascotaDAO.java
│   │   ├── Veterinario.java
│   │   ├── VeterinarioDAO.java
│   │   ├── Especialidad.java
│   │   ├── EspecialidadDAO.java
│   │   └── DashboardDAO.java
│   ├── vista/
│   │   ├── Login.java
│   │   ├── Menu.java
│   │   ├── Panel_DashboardInicial.java
│   │   ├── Panel_GestionarCitas.java
│   │   ├── Panel_GestionarMascotas.java
│   │   ├── Panel_GestionarUsuarios.java
│   │   ├── Panel_RegistrarCitas.java
│   │   ├── Panel_RegistrarConsulta.java
│   │   ├── Panel_RegistrarUsuario.java
│   │   └── dashboard/
│   │       ├── DashboardAdministrador.java
│   │       ├── DashboardAdministrativo.java
│   │       ├── DashboardCliente.java
│   │       └── DashboardVeterinario.java
│   ├── utils/
│   │   ├── Conexion.java
│   │   └── Sesion.java
│   └── documentacion/
│       ├── Login_ChronoVet.png
│       └── Diagrama_ER.png
├── db/
│   └── ChronoVet.sql
├── lib/
│   └── *.jar
└── README.md
```
---

## 🚀 Funcionalidades Implementadas

- ✅ Autenticación de usuarios con roles
- ✅ CRUD completo de usuarios (Administrador)
- ✅ CRUD de mascotas
- ✅ Agendamiento de citas con validación de horario
- ✅ Asignación automática de veterinarios disponibles
- ✅ Registro de consultas médicas
- ✅ Cancelación de citas (mínimo 3 horas de anticipación)
- ✅ Dashboards dinámicos por rol
- ✅ Auditoría de cambios en citas
- ✅ Manejo de ausencias de veterinarios
- ✅ Validación de límite de 3 citas por día por usuario

---

## 📋 Reglas de Negocio

| ID | Regla |
|----|--------|
| RN-01 | Una mascota no puede tener dos citas pendientes que se solapen en el tiempo. |
| RN-02 | Una cita solo puede cancelarse con al menos 3 horas de anticipación. |
| RN-03 | La mascota debe pertenecer al usuario que agenda la cita. |
| RN-04 | La cita se asigna automáticamente a un veterinario disponible de la especialidad requerida. |
| RN-05 | Un veterinario no puede tener dos citas simultáneas o solapadas. |
| RN-06 | La asignación de veterinarios considera ausencias registradas. |
| RN-07 | El horario de atención es de lunes a sábado entre las 08:00 y las 17:00 horas. |
| RN-08 | Un usuario no puede registrar más de 3 citas en un mismo día. |
| RN-09 | Solo el veterinario asignado puede registrar la consulta médica asociada a la cita. |
| RN-10 | Solo veterinarios activos y disponibles pueden ser asignados a nuevas citas. |

---

## 📸 Capturas de Pantalla

### Pantalla de Inicio de Sesión

![Login ChronoVet](src/documentacion/Login%20ChronoVet.png)

---

## 📄 Licencia

Este proyecto fue desarrollado con fines académicos como parte del curso **Tecnología de Desarrollo de Sistemas Informáticos**.

Su propósito principal es el aprendizaje y aplicación de conceptos relacionados con:

- Programación orientada a objetos en Java
- Arquitectura MVC
- Desarrollo de aplicaciones de escritorio con Swing
- Diseño y administración de bases de datos PostgreSQL
- Procedimientos almacenados, funciones y triggers

---

## 👨‍💻 Autor

### Jhonatan Motta

[jhonatanmotta](https://github.com/jhonatanmotta)

---

## ⭐ Apóyame

Si este proyecto te resulta útil o interesante, considera darle una estrella en GitHub.

⭐ **¡Gracias por visitar ChronoVet!**
