# ParkingJOlea

[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=Pokoion_ParkingJOlea&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Pokoion_ParkingJOlea)

ParkingJOlea es una aplicación Android desarrollada como proyecto de aula de empresa para LKS. El objetivo de la app es gestionar reservas de plazas de parking para trabajadores de Zuatzu, permitiendo la autenticación de usuarios, la creación y gestión de reservas, y la visualización del histórico y reservas actuales.

## Características principales

- Registro y login de usuarios.
- Recuperación y cambio de contraseña.
- Creación de reservas de parking con selección de fecha, hora de inicio y fin, y tipo de reserva.
- Validación de intervalos de tiempo y reglas de negocio (máximo 9 horas, no reservar en el pasado, etc.).
- Visualización de reservas actuales y pasadas.
- Notificaciones sobre reservas (15 minutos antes de que acabe y 30 minutos antes de que empiece la reserva).
- Interfaz moderna y adaptada a dispositivos móviles.

## Estructura del proyecto

- `app/src/main/java/com/lksnext/parkingplantilla/`
  - `adapters/`: Adaptadores para listas y vistas.
  - `data/`: Modelos de datos y acceso a datos.
  - `domain/`: Lógica de negocio.
  - `notifications/`: Gestión de notificaciones.
  - `utils/`: Utilidades y validadores.
  - `view/`
    - `activity/`: Pantallas principales (Login, Registro, Main, Cambio de contraseña).
    - `fragment/`: Fragmentos para reservas, usuario, histórico, etc.
  - `viewmodel/`: ViewModels para arquitectura MVVM.
  - `ParkingApplication.java`: Clase de aplicación principal.

- `app/src/main/res/`
  - `layout/`: Archivos XML de interfaz de usuario (pantallas y fragmentos).
  - `drawable/`, `mipmap-*`: Recursos gráficos.
  - `values/`: Strings, estilos y colores.

## CI/CD y calidad del código

El proyecto utiliza **GitHub Actions** para CI/CD, integrando análisis de calidad con **SonarCloud** en cada push y pull request. Esto permite monitorizar la calidad del código de forma continua.

La intención era automatizar también la ejecución de los tests instrumentados (`androidTest`) en el pipeline, pero actualmente no se ha conseguido debido a las limitaciones para lanzar emuladores Android en GitHub Actions. Por tanto, los tests deben ejecutarse manualmente desde Android Studio.

## Requisitos técnicos

- Android Studio (recomendado versión Hedgehog o superior)
- Gradle (configurado en el proyecto)
- Java 8 o superior
- Conexión a internet para autenticación y sincronización (si se conecta a backend)
- Dependencias gestionadas en `build.gradle`
- Google Services configurado (`google-services.json` incluido)

## Instalación y ejecución

1. Clona este repositorio:
   ```
   git clone https://github.com/Pokoion/ParkingJOlea.git
   ```
2. Abre el proyecto en Android Studio.
3. Sincroniza el proyecto con Gradle.
4. Conecta un dispositivo o usa un emulador.
5. Ejecuta la app.

## Uso

- Regístrate con tu correo corporativo.
- Inicia sesión.
- Crea una reserva seleccionando fecha, hora y tipo.
- Consulta tus reservas actuales y el histórico.
- Recibe notificaciones sobre tus reservas.

## Validaciones y reglas de negocio

- No se pueden hacer reservas en el pasado.
- El intervalo máximo de una reserva es de 9 horas.
- No se permiten campos vacíos en el registro/login.
- Validación de email y contraseña segura.
- Solo se puede reservar si hay plazas disponibles (lógica a implementar según backend).
- Solo se permite crear una reserva por usuario al día, y si la reserva finaliza no se puede crear otra para el mismo dia.

## Arquitectura de datos

La gestión y acceso a los datos de la aplicación se centraliza en la clase `DataRepository` (ubicada en `data/`). Esta clase actúa como intermediario entre las fuentes de datos (remotas o locales) y los ViewModels, proporcionando una única interfaz para obtener y actualizar la información relevante de reservas y usuarios. De este modo, se facilita la separación de responsabilidades y la escalabilidad del proyecto.

## Proceso de desarrollo

El desarrollo de la app se ha realizado siguiendo una metodología estructurada y buenas prácticas de control de versiones:

1. **Diseño de interfaces**: El primer paso fue la creación de las interfaces de usuario (pantallas y fragmentos) en XML, definiendo la experiencia visual y los flujos principales de la app.
2. **Estructura general**: Se organizó el proyecto en paquetes siguiendo el patrón MVVM, separando claramente la lógica de presentación, negocio y acceso a datos.
3. **Implementación local de DataRepository/DataSource**: Inicialmente, se implementó un repositorio de datos local para simular la gestión de reservas y usuarios, facilitando el desarrollo y pruebas sin depender de servicios externos.
4. **Integración con Firebase**: Posteriormente, se migró la gestión de datos a Firebase, utilizando:
   - **Firebase Authentication** para la gestión de usuarios y autenticación segura.
   - **Cloud Firestore Database** como base de datos principal para almacenar reservas y usuarios.
   - **Firebase Functions** para actualizar el estado de las reservas automáticamente, sin depender de que haya una instancia activa de la app.

Durante el desarrollo se ha utilizado (o intentado utilizar) la estrategia de ramas **gitflow** para organizar el trabajo, facilitando la colaboración y el control de versiones.

## Tests

El proyecto incluye tests instrumentados en la carpeta `androidTest`, centrados en la comprobación de ViewModels y Fragments principales de la aplicación. Estos tests permiten validar el comportamiento de la lógica de presentación y la interacción de los componentes de la interfaz de usuario en un entorno real de Android.

Para ejecutar los tests:

1. Abre el proyecto en Android Studio.
2. Haz clic derecho sobre la carpeta `androidTest` o sobre una clase de test específica.
3. Selecciona "Run tests" para ejecutar los tests instrumentados y ver los resultados en el dispositivo o emulador.

## Integración continua y calidad del código

Este proyecto utiliza SonarCloud para el análisis de calidad del código y seguimiento de métricas.

- [Ver análisis en SonarCloud](https://sonarcloud.io/project/overview?id=Pokoion_ParkingJOlea)

El Quality Gate está configurado para permitir coverage 0%, ya que SonarCloud solo recoge cobertura de tests unitarios (carpeta `test`) y en este proyecto únicamente existen tests instrumentados (carpeta `androidTest`), que no generan métricas de cobertura para SonarCloud. Por ello, el coverage mostrado es 0%, aunque sí existen pruebas automáticas para ViewModels y Fragments. El resto de métricas de calidad pueden consultarse en el enlace anterior.

## Autor

Desarrollado por Jon Olea para el aula de empresa de LKS.

## Licencia

Proyecto académico para LKS. Uso restringido a fines educativos.

