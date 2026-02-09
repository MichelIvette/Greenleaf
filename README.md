<p align="center">
  <img src="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Logo.png" width="300">
</p>

<p align="center"> Aplicación móvil para una tienda de plantas Greenleaf (ficticia)</p>

## Descripción

**Greenleaf** es una aplicación móvil desarrollada en Android Studio, que funciona como una aplicación de compra en línea con opción de recogida en tienda por el momento, incorporando un catálogo interactivo de plantas para la tienda especializada “Greenleaf”, con funcionalidades básicas de administración. 

---

## Requisitos del Sistema

**Software necesario:**
- Android Studio (versión reciente)
- JetBrains Runtime (JDK 21 incluido con Android Studio)
- Firebase 
- Gradle (incluido en Android Studio)
- Git + GitHub
- Emulador / dispositivo físico

## Manual de usuario

| Inicio | Crear cuenta | Permisos |
|------|--------|-----------|
| <a href="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Inicio.PNG"><img src="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Inicio.PNG" width="200"></a> | <a href="https://github.com/MichelIvette/Greenleaf/blob/main/Images/NuevaCuenta.PNG"><img src="https://github.com/MichelIvette/Greenleaf/blob/main/Images/NuevaCuenta.PNG" width="200"></a> | <a href="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Permisos.PNG"><img src="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Permisos.PNG" width="200"></a> | 

**1. Pantalla de bienvenida**
- Elemento: Imagen de fondo con logo y nombre de la app.
- Descripción: Pantalla de entrada que da la bienvenida al usuario antes de iniciar sesión o registrarse.
- Instrucción para el usuario: Espera unos segundos para ser redirigido automáticamente a la pantalla de inicio de sesión o toca para continuar más rápido.
**2. Inicio de sesión**
- Elemento: Campos de texto para correo electrónico y contraseña + botón de "Entrar".
- Descripción: Permite a los usuarios registrados acceder a su cuenta.
- Instrucción para el usuario:
Escribe tu correo electrónico y contraseña.
Toca el botón “Iniciar sesión” para entrar a tu cuenta.
Si olvidaste tu contraseña, selecciona “¿Olvidaste tu contraseña?” para recuperarla.
**3. Enlace para crear una nueva cuenta como cliente**
- Elemento: Enlace “¿No tienes cuenta? Registrate como cliente”.
- Descripción: Permite a nuevos usuarios crear una cuenta.
- Instrucción para el usuario: Presiona el enlace para que te redireccione a la pantalla de crear nueva cuenta.
**4. Registro de usuario**
- Elemento: Formulario con campos personales.
- Descripción: Crea una cuenta nueva.
- Instrucción para el usuario: Llena todos los campos y toca el botón “Registrar”.
**5. Tomar fotografía**
- Elemento: Botón para tomar fotografía.
- Descripción: Acceso a la cámara.
- Instrucción para el usuario: Presiona el botón “Tomar fotografía” para que te puedas tomar una foto.
**6. Enlace para regresar a la pantalla de inicio de sesión**
- Elemento: Enlace “¿Ya tienes cuenta? Inicia sesión”.
- Descripción: Permite regresar a la pantalla de inicio de sesión.
- Instrucción para el usuario: Presiona el enlace para que te redireccione a la pantalla de inicio de sesión.
**7. Permisos de cámara**
- Elemento: Ventana emergente con solicitud de acceso a la cámara.
- Descripción: La app requiere acceso a la cámara para escanear plantas o subir fotos para recibir sugerencias.
- Instrucción para el usuario: Toca “Permitir” si deseas tomar fotos desde la app y para subir imágenes desde la galería.
**8. Permisos de ubicación**
- Elemento: Ventana emergente con solicitud de permiso de ubicación.
- Descripción: La app solicita acceso a la ubicación del dispositivo para ofrecer recomendaciones personalizadas según la zona del usuario.
- Instrucción para el usuario: Selecciona “Permitir solo con la app en uso” para que la app funcione correctamente. Esto ayudará a encontrar plantas adecuadas para tu clima o región.


### Perfil del cliente

| <a href="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Usuario.PNG"><img srchttps://github.com/MichelIvette/Greenleaf/blob/main/Images/Usuario.PNG" width="300"></a> | <a href="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Usuario1.PNG"><img src="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Usuario1.PNG" width="300"></a> | <a href="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Usuario2.PNG"><img src="https://github.com/MichelIvette/Greenleaf/blob/main/Images/Usuario2.PNG" width="300"></a> | 


**9. Encabezado de promoción**
- Elemento: Banner superior verde.
- Descripción: Muestra promociones destacadas o novedades.
- Instrucción para el usuario: Al tocar el banner, el cliente será redirigido a los detalles de la planta promocionada.
**10. Buscador de plantas**
- Elemento: Barra de búsqueda con ícono de lupa.
- Descripción: Permite buscar plantas por nombre.
- Instrucción para el usuario: Escribe el nombre o parte del nombre de una planta para filtrar el catálogo.
**11. Orden alfabético**
- Elemento: Botón con ícono A-Z.
- Descripción: Ordena las plantas alfabéticamente.
- Instrucción para el usuario: Toca el ícono para cambiar el orden de A-Z o Z-A.
**12. Filtros por características**
- Elemento: Botones redondos con íconos (riego, estancia, luz solar, sombra, interior, exterior, etc.).
- Descripción: Permite filtrar las plantas por condiciones de cuidado.
- Instrucción para el usuario: Toca un ícono para filtrar el catálogo según las necesidades específicas (ej. sol directo, sombra parcial).
**13. Catálogo de plantas**
- Elemento: Tarjetas con foto, precio, nombre y breve descripción.
- Descripción: Muestra las plantas disponibles en la tienda.
- Instrucción para el usuario:
Toca una tarjeta para ver la información detallada.
El ícono de corazón permite agregar o quitar de la lista de favoritos.
**14. Menú de navegación inferior**
- Botones: Inicio, Pedidos, Perfil, Favoritos.
- Descripción: Permite desplazarse a otras secciones principales de la app.
- Instrucción para el usuario:
Inicio: Regresa a esta pantalla.
Pedidos: Consulta tus pedidos.
Perfil: Accede a tus datos personales y configuración.
Favoritos: Consulta la lista de plantas marcadas como favoritas.
**15. Detalles de la planta**
- Elemento: Imagen, descripción y botón “Agregar al carrito”.
- Descripción: Muestra la información completa.
- Instrucción: Revisa detalles o agrega al carrito al presionar el botón “Añadir al carrito”.










