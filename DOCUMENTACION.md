# Documentación Técnica — EconFineShop
**Versión:** 1.0  
**Plataforma:** Android (Kotlin)  
**Fecha:** Mayo 2026  

---

## Tabla de contenidos

1. [Descripción general](#1-descripción-general)
2. [Tecnologías y dependencias](#2-tecnologías-y-dependencias)
3. [Arquitectura](#3-arquitectura)
4. [Estructura de paquetes](#4-estructura-de-paquetes)
5. [Funcionalidades](#5-funcionalidades)
6. [Modelos de datos](#6-modelos-de-datos)
7. [Capa de repositorios](#7-capa-de-repositorios)
8. [Capa de ViewModels](#8-capa-de-viewmodels)
9. [Pantallas](#9-pantallas)
10. [Navegación](#10-navegación)
11. [Base de datos — Firestore](#11-base-de-datos--firestore)
12. [Autenticación](#12-autenticación)
13. [Flujo de compra](#13-flujo-de-compra)
14. [Sistema de colores](#14-sistema-de-colores)
15. [Sistema de notificaciones](#15-sistema-de-notificaciones)
16. [Panel de administrador](#16-panel-de-administrador)
17. [Sistema de puntos ecológicos](#17-sistema-de-puntos-ecológicos)
18. [Sesión de usuario — AppSession](#18-sesión-de-usuario--appsession)
19. [Configuración del proyecto](#19-configuración-del-proyecto)
20. [Decisiones técnicas importantes](#20-decisiones-técnicas-importantes)

---

## 1. Descripción general

**EconFineShop** es una aplicación de comercio electrónico Android orientada a la venta de productos ecológicos. Permite a los usuarios explorar un catálogo de productos, agregar artículos al carrito, gestionar direcciones de envío y métodos de pago, y realizar pedidos. La aplicación incluye un sistema de gamificación mediante puntos ecológicos y retos ambientales para incentivar el consumo responsable.

### Características principales
- Catálogo de productos con categorías, búsqueda y filtros
- Carrito de compras persistente en sesión
- Flujo de confirmación de pedido con selección de dirección y método de pago
- Historial de pedidos sincronizado con la nube
- Direcciones y métodos de pago guardados en la nube por usuario
- Sistema de puntos ecológicos y retos
- Lista de deseos (wishlist)
- Notificaciones internas del sistema
- Panel de administrador para gestión de productos y pedidos

---

## 2. Tecnologías y dependencias

### Lenguaje y plataforma
| Elemento | Versión |
|---|---|
| Lenguaje | Kotlin |
| SDK mínimo | API 24 (Android 7.0) |
| SDK objetivo | API 34 (Android 14) |
| Compile SDK | 34 |

### Dependencias principales

| Librería | Versión | Uso |
|---|---|---|
| AndroidX Core KTX | 1.12.0 | Extensiones Kotlin para Android |
| AppCompat | 1.6.1 | Compatibilidad hacia atrás |
| Material Design | 1.11.0 | Componentes UI (botones, cards, dialogs, chips) |
| Navigation Component | 2.7.5 | Navegación entre fragmentos |
| ViewModel + LiveData | 2.6.2 | Arquitectura MVVM |
| RecyclerView | 1.3.2 | Listas de productos, pedidos, etc. |
| ViewPager2 | 1.0.0 | Banner de promociones en Home |
| DotsIndicator | 5.0 | Indicador de página del banner |
| Glide | 4.16.0 | Carga de imágenes desde URL |
| Gson | 2.10.1 | Serialización JSON (SharedPreferences legacy) |
| Firebase BOM | 32.7.4 | Gestión de versiones Firebase |
| Firebase Auth KTX | — | Autenticación de usuarios |
| Firebase Firestore KTX | — | Base de datos en la nube |
| Coroutines Android | 1.7.3 | Operaciones asíncronas |
| Shimmer | 0.5.0 | Efecto de carga esqueleto |
| SwipeRefreshLayout | 1.1.0 | Actualización por deslizamiento |
| kotlin-parcelize | — | Serialización de objetos entre fragmentos |

---

## 3. Arquitectura

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** con una arquitectura de **Single Activity + múltiples Fragments**.

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                          │
│  Activities: SplashActivity, AuthActivity,           │
│              MainActivity                            │
│  Fragments: 20+ fragmentos de pantalla               │
└───────────────────────┬─────────────────────────────┘
                        │  observa LiveData
┌───────────────────────▼─────────────────────────────┐
│                 ViewModel Layer                      │
│  ShopViewModel — estado de la tienda                 │
│  AdminViewModel — operaciones del panel admin        │
│  AuthViewModel — autenticación                       │
└───────────────────────┬─────────────────────────────┘
                        │  llama métodos
┌───────────────────────▼─────────────────────────────┐
│               Repository Layer                       │
│  Local: ShopRepository, UserRepository,              │
│         AddressRepository, PaymentRepository,        │
│         NotificationRepository, EcoRepository        │
│  Firebase: FirebaseAuthRepository,                   │
│            FirestoreProductRepository,               │
│            FirestoreOrderRepository,                 │
│            FirestoreUserRepository,                  │
│            FirestoreAddressRepository,               │
│            FirestorePaymentRepository                │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│               Data Layer                             │
│  Firebase Firestore (nube)                           │
│  SharedPreferences (local — sesión y caché)          │
└─────────────────────────────────────────────────────┘
```

### Principios aplicados
- **Separación de responsabilidades:** cada capa solo conoce a la capa inmediatamente inferior
- **Single source of truth:** Firestore es la fuente principal para productos, pedidos, direcciones y pagos
- **Observabilidad:** los fragmentos observan LiveData del ViewModel sin lógica de negocio directa
- **View Binding:** todas las vistas usan View Binding para acceso seguro a los elementos del layout

---

## 4. Estructura de paquetes

```
com.shopapp/
├── AppSession.kt                     # Singleton de sesión activa
│
├── data/
│   ├── model/                        # Modelos de datos (data classes)
│   │   ├── Address.kt
│   │   ├── AppNotification.kt
│   │   ├── CartItem.kt
│   │   ├── Category.kt
│   │   ├── EcoChallenge.kt
│   │   ├── Order.kt
│   │   ├── PaymentMethod.kt
│   │   ├── Product.kt
│   │   └── User.kt
│   │
│   └── repository/                   # Repositorios de datos
│       ├── AddressRepository.kt          (local — legacy)
│       ├── EcoRepository.kt
│       ├── FirebaseAuthRepository.kt
│       ├── FirestoreAddressRepository.kt (Firestore)
│       ├── FirestoreOrderRepository.kt   (Firestore)
│       ├── FirestorePaymentRepository.kt (Firestore)
│       ├── FirestoreProductRepository.kt (Firestore)
│       ├── FirestoreUserRepository.kt    (Firestore)
│       ├── NotificationRepository.kt
│       ├── PaymentRepository.kt          (local — legacy)
│       ├── ShopRepository.kt
│       └── UserRepository.kt
│
├── ui/
│   ├── admin/                        # Panel de administrador
│   │   ├── AdminAddEditProductFragment.kt
│   │   ├── AdminDashboardFragment.kt
│   │   ├── AdminOrdersAdapter.kt
│   │   ├── AdminOrdersFragment.kt
│   │   ├── AdminProductAdapter.kt
│   │   └── AdminProductsFragment.kt
│   │
│   ├── auth/                         # Autenticación
│   │   ├── AuthActivity.kt
│   │   ├── LoginFragment.kt
│   │   └── RegisterFragment.kt
│   │
│   ├── cart/                         # Carrito
│   │   ├── CartAdapter.kt
│   │   └── CartFragment.kt
│   │
│   ├── checkout/                     # Confirmación de pedido
│   │   └── CheckoutFragment.kt
│   │
│   ├── eco/                          # Retos ecológicos
│   │   └── EcoChallengesFragment.kt
│   │
│   ├── home/                         # Pantalla principal
│   │   ├── HomeFragment.kt
│   │   ├── MainActivity.kt
│   │   ├── SearchFragment.kt
│   │   └── adapter/
│   │       ├── BannerAdapter.kt
│   │       ├── CategoryAdapter.kt
│   │       └── ProductAdapter.kt
│   │
│   ├── product/                      # Detalle de producto
│   │   ├── CategoryProductsFragment.kt
│   │   ├── ProductDetailFragment.kt
│   │   └── SizeColorAdapters.kt
│   │
│   ├── profile/                      # Perfil y secciones
│   │   ├── AddressesFragment.kt
│   │   ├── EditProfileFragment.kt
│   │   ├── HelpFragment.kt
│   │   ├── NotificationsFragment.kt
│   │   ├── OrdersAdapter.kt
│   │   ├── OrdersFragment.kt
│   │   ├── PaymentMethodsFragment.kt
│   │   ├── ProfileFragment.kt
│   │   └── WishlistFragment.kt
│   │
│   └── splash/
│       └── SplashActivity.kt
│
├── util/
│   ├── ColorConstants.kt             # Colores disponibles para productos
│   └── PriceFormatter.kt            # Extensión toCOP()
│
└── viewmodel/
    ├── AdminViewModel.kt
    ├── AuthViewModel.kt
    └── ShopViewModel.kt
```

---

## 5. Funcionalidades

### Para el usuario cliente

| Funcionalidad | Descripción |
|---|---|
| **Splash** | Pantalla de bienvenida con verificación automática de sesión |
| **Registro** | Creación de cuenta con nombre, correo y contraseña (Firebase Auth) |
| **Inicio de sesión** | Acceso con correo y contraseña |
| **Home** | Banners de promoción, categorías, productos destacados y populares |
| **Búsqueda** | Búsqueda en tiempo real por nombre de producto |
| **Categorías** | Filtro de productos por categoría |
| **Detalle de producto** | Fotos, descripción, precio, selector de talla y color, botón de agregar al carrito |
| **Lista de deseos** | Guardar productos favoritos con el ícono de corazón |
| **Carrito** | Ver artículos, cambiar cantidades, eliminar, ver subtotal + envío + total |
| **Checkout** | Seleccionar dirección y método de pago antes de confirmar el pedido |
| **Mis pedidos** | Historial de pedidos con estado, fecha, total y cantidad de artículos |
| **Mis direcciones** | CRUD completo de direcciones de envío, sincronizadas en Firestore |
| **Métodos de pago** | CRUD de tarjetas, PSE y efectivo con contraentrega, sincronizados en Firestore |
| **Editar perfil** | Cambiar nombre, teléfono y contraseña |
| **Retos ecológicos** | Ver y completar retos ambientales que otorgan puntos |
| **Notificaciones** | Centro de notificaciones del sistema con navegación contextual |
| **Ayuda** | Sección de preguntas frecuentes y datos de contacto |

### Para el administrador

| Funcionalidad | Descripción |
|---|---|
| **Panel Admin** | Dashboard con acceso rápido a inventario y pedidos |
| **Gestión de productos** | Agregar, editar y eliminar productos del catálogo |
| **Gestión de pedidos** | Ver todos los pedidos y cambiar su estado |
| **Notificaciones automáticas** | Se generan automáticamente al agregar, editar o eliminar productos |

---

## 6. Modelos de datos

### Product
```kotlin
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val imageUrl: String,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val inStock: Boolean = true,
    val isNew: Boolean = false,
    val discount: Int = 0,          // Porcentaje de descuento (0–100)
    val isFavorite: Boolean = false,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),  // Nombres de colores (ej: "Rojo", "Azul")
    val ecoPoints: Int = 0
)
```

### CartItem
```kotlin
data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedSize: String = "",
    val selectedColor: String = ""
)
```

### Order
```kotlin
data class Order(
    val id: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val status: OrderStatus,        // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    val createdAt: Date,
    val address: String,
    val ecoPointsEarned: Int
)
```

### Address
```kotlin
data class Address(
    val id: String,
    val label: String,              // "Casa", "Trabajo", "Otro"
    val recipientName: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String,
    val isDefault: Boolean
)
```

### PaymentMethod
```kotlin
data class PaymentMethod(
    val id: String,
    val type: PaymentType,          // CREDIT, DEBIT, PSE, CASH_ON_DELIVERY
    val cardHolder: String,
    val lastFour: String,
    val brand: CardBrand,           // VISA, MASTERCARD, AMEX, OTHER
    val expiryMonth: String,
    val expiryYear: String,
    val bankName: String,           // Solo para PSE
    val isDefault: Boolean
)
```

### AppNotification
```kotlin
data class AppNotification(
    val id: String,
    val type: NotificationType,     // ORDER, PROMO, ECO, PRODUCT
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val targetType: String?,        // "order", "product", "category" (para navegación)
    val targetId: String?
)
```

### User
```kotlin
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val ecoPoints: Int,
    val role: String                // "customer" o "admin"
)
```

### EcoChallenge
Representa un reto ecológico con nombre, descripción, puntos requeridos e ícono. El progreso se calcula dinámicamente comparando los puntos y compras del usuario.

---

## 7. Capa de repositorios

### Repositorios locales (SharedPreferences / Memoria)

| Repositorio | Almacenamiento | Responsabilidad |
|---|---|---|
| `ShopRepository` | Memoria (List en objeto) | Productos, carrito, wishlist, pedidos de sesión, categorías, banners |
| `UserRepository` | SharedPreferences | Datos de sesión local: nombre, email, teléfono, puntos eco |
| `NotificationRepository` | SharedPreferences + Gson | Lista de notificaciones internas |
| `EcoRepository` | Lógica en memoria | Generación de retos según progreso del usuario |
| `AddressRepository` | SharedPreferences (legacy) | Solo se mantiene por compatibilidad, ya no es la fuente principal |
| `PaymentRepository` | SharedPreferences (legacy) | Solo se mantiene por compatibilidad, ya no es la fuente principal |

### Repositorios Firestore (Nube)

| Repositorio | Colección Firestore | Responsabilidad |
|---|---|---|
| `FirebaseAuthRepository` | Firebase Auth | Login, registro, logout, cambio de contraseña |
| `FirestoreUserRepository` | `users/{uid}` | Perfil de usuario, rol, puntos eco, contador de compras |
| `FirestoreProductRepository` | `products/{productId}` | Catálogo de productos; sincroniza con datos locales |
| `FirestoreOrderRepository` | `orders/{orderId}` | Guardar y consultar pedidos por usuario o todos (admin) |
| `FirestoreAddressRepository` | `users/{uid}/addresses/{id}` | CRUD de direcciones por usuario |
| `FirestorePaymentRepository` | `users/{uid}/paymentMethods/{id}` | CRUD de métodos de pago por usuario |

---

## 8. Capa de ViewModels

### ShopViewModel
ViewModel principal compartido entre todos los fragmentos de la tienda. Se instancia una sola vez en `MainActivity`.

**LiveData expuestos:**
- `products`, `featuredProducts`, `popularProducts`, `newProducts` — listas de productos
- `categories` — lista de categorías
- `cartItems`, `cartCount`, `cartTotal` — estado del carrito
- `wishlistProducts` — lista de deseos
- `banners` — banners del home
- `currentUser` — usuario en sesión
- `orderPlaced` — señal de pedido confirmado (se consume una vez)
- `ecoPoints`, `ecoChallenges` — datos ecológicos
- `message` — mensajes temporales para mostrar en UI
- `searchResults`, `categoryProducts` — resultados de búsqueda y filtro

**Acciones principales:**
- `addToCart()`, `removeFromCart()`, `updateCartQuantity()`
- `toggleWishlist()`, `isInWishlist()`
- `placeOrder()` — confirma el pedido, actualiza Firestore y genera notificación
- `searchProducts()`, `loadCategoryProducts()`
- `reloadProductsFromFirestore()` — usado por admin después de cambios

### AdminViewModel
Maneja las operaciones del panel de administrador.

**Acciones:**
- `addProduct(product)` — agrega a Firestore y genera notificación
- `updateProduct(product)` — actualiza en Firestore, detecta cambios (precio, colores, stock, descuento) y genera notificación descriptiva
- `deleteProduct(productId)` — elimina de Firestore y genera notificación
- `loadProducts()`, `loadOrders()` — carga datos para las listas admin
- `updateOrderStatus(orderId, status)` — cambia el estado de un pedido

### AuthViewModel
Maneja el flujo de autenticación.

**Acciones:**
- `login(email, password)` — autentica con Firebase, detecta si es admin por email
- `register(name, email, password, phone)` — crea cuenta en Firebase y perfil en Firestore
- `logout()` — cierra sesión

---

## 9. Pantallas

### Activities

| Activity | Rol |
|---|---|
| `SplashActivity` | Punto de entrada. Verifica si hay sesión activa y redirige a AuthActivity o MainActivity |
| `AuthActivity` | Contenedor para LoginFragment y RegisterFragment |
| `MainActivity` | Activity principal. Contiene el NavHostFragment y la barra de navegación inferior. Expone funciones de navegación para todos los fragmentos |

### Fragments — Tienda

| Fragment | Descripción |
|---|---|
| `HomeFragment` | Home con banners, categorías y secciones de productos. Muestra nombre del usuario y tagline ecológico |
| `SearchFragment` | Búsqueda en tiempo real con campo de texto y lista de resultados |
| `ProductDetailFragment` | Detalle completo: imagen, precio, tallas (chips), colores (circles + nombre), cantidad, botón agregar al carrito |
| `CategoryProductsFragment` | Lista de productos filtrada por categoría |
| `CartFragment` | Lista de items del carrito con totales. Botón "Pagar" navega al Checkout |
| `CheckoutFragment` | Selección de dirección y método de pago antes de confirmar. Carga ambas listas desde Firestore en paralelo |

### Fragments — Perfil

| Fragment | Descripción |
|---|---|
| `ProfileFragment` | Vista del perfil con nombre, email, avatar, estadísticas y menú de opciones |
| `EditProfileFragment` | Edición de nombre, teléfono y contraseña. Avatar con inicial del nombre |
| `OrdersFragment` | Historial de pedidos cargado desde Firestore |
| `AddressesFragment` | CRUD de direcciones de envío sincronizado con Firestore |
| `PaymentMethodsFragment` | CRUD de métodos de pago (Tarjeta, PSE, Efectivo) sincronizado con Firestore |
| `WishlistFragment` | Lista de productos guardados como favoritos |
| `NotificationsFragment` | Centro de notificaciones con navegación contextual al tocar una notificación |
| `HelpFragment` | Preguntas frecuentes y datos de contacto de soporte |
| `EcoChallengesFragment` | Retos ecológicos con barra de progreso |

### Fragments — Admin

| Fragment | Descripción |
|---|---|
| `AdminDashboardFragment` | Panel principal con accesos a inventario y pedidos |
| `AdminProductsFragment` | Lista de todos los productos con opciones de editar y eliminar |
| `AdminAddEditProductFragment` | Formulario para agregar o editar un producto. Incluye selector de colores con chips visuales |
| `AdminOrdersFragment` | Lista de todos los pedidos con opción de cambiar estado |

---

## 10. Navegación

La navegación se gestiona con **Navigation Component** y un único grafo declarado en `res/navigation/nav_graph.xml`. El punto de inicio es `homeFragment`.

`MainActivity` centraliza todas las funciones de navegación:

```kotlin
// Tienda
navigateToProductDetail(productId)
navigateToCategoryProducts(categoryName)
navigateToCheckout()

// Perfil
navigateToEditProfile()
navigateToOrders()
navigateToWishlist()
navigateToAddresses()
navigateToPaymentMethods()
navigateToNotifications()
navigateToHelp()

// Admin
navigateToAdminDashboard()
navigateToAdminProducts()
navigateToAdminOrders()
navigateToAdminAddEditProduct(productId)

navigateUp()  // Volver atrás
```

La barra de navegación inferior (Bottom Navigation) es visible solo en las 5 pantallas principales: Home, Búsqueda, Carrito, Retos Eco y Perfil.

---

## 11. Base de datos — Firestore

### Estructura de colecciones

```
firestore/
│
├── products/
│   └── {productId}/
│       ├── id, name, description, price, originalPrice
│       ├── imageUrl, category, rating, reviewCount
│       ├── inStock, isNew, discount, ecoPoints
│       ├── sizes: [String]
│       └── colors: [String]   ← nombres en español ("Rojo", "Azul", etc.)
│
├── orders/
│   └── {orderId}/
│       ├── id, userId, userEmail
│       ├── totalAmount, status, createdAt (timestamp)
│       ├── address, ecoPointsEarned, itemCount
│       └── items: [{productId, productName, quantity, unitPrice,
│                    totalPrice, imageUrl, category,
│                    selectedSize, selectedColor}]
│
└── users/
    └── {uid}/
        ├── uid, name, email, role ("customer" | "admin")
        ├── ecoPoints, purchaseCount
        │
        ├── addresses/
        │   └── {addressId}/
        │       ├── id, label, recipientName
        │       ├── street, city, state, zipCode
        │       ├── phone, isDefault
        │
        └── paymentMethods/
            └── {methodId}/
                ├── id, type ("CREDIT"|"DEBIT"|"PSE"|"CASH_ON_DELIVERY")
                ├── cardHolder, lastFour, brand, expiryMonth, expiryYear
                ├── bankName (solo PSE)
                └── isDefault
```

### Estrategia de sincronización de productos

Al iniciar la app, `ShopViewModel` sigue este flujo:
1. Carga los datos locales de `ShopRepository` para respuesta inmediata
2. Consulta Firestore en segundo plano
3. Si Firestore tiene productos → reemplaza los datos locales
4. Si Firestore está vacío (primera vez) → sube los productos locales a Firestore

---

## 12. Autenticación

Se usa **Firebase Authentication** con el método Email/Password.

### Flujo de login
1. El usuario ingresa correo y contraseña
2. `AuthViewModel.login()` llama a `FirebaseAuthRepository`
3. Si el correo coincide con el email de administrador → `AppSession.userRole = "admin"` (sin esperar Firestore)
4. Si es usuario normal → consulta `FirestoreUserRepository.getUserRole()` para obtener el rol
5. Se navega a `MainActivity`

### Identificación del administrador
El administrador se identifica por su dirección de correo electrónico, definida como constante en `FirestoreUserRepository.ADMIN_EMAIL`. Este bypass evita depender de la disponibilidad de Firestore para el acceso admin.

### Logout
Al cerrar sesión:
1. `FirebaseAuthRepository.logout()` — cierra la sesión en Firebase
2. `AppSession.clear()` — limpia el estado en memoria
3. `UserRepository.logout()` — borra los datos locales de SharedPreferences
4. Se navega a `AuthActivity` limpiando el back stack

---

## 13. Flujo de compra

```
[ProductDetailFragment]
        │ "Agregar al carrito"
        ▼
[CartFragment]
        │ Botón "Pagar"
        ▼
[CheckoutFragment]
        ├── Carga direcciones desde Firestore
        ├── Carga métodos de pago desde Firestore
        ├── Usuario selecciona dirección ──► Si no tiene, navega a AddressesFragment
        ├── Usuario selecciona método    ──► Si no tiene, navega a PaymentMethodsFragment
        │
        │ Botón "Confirmar pedido" (habilitado solo cuando ambos están seleccionados)
        ▼
[ShopViewModel.placeOrder()]
        ├── Guarda pedido en Firestore
        ├── Suma puntos eco al usuario (local + Firestore)
        ├── Genera notificación interna
        └── Limpia el carrito
        ▼
[Toast con confirmación + puntos ganados]
        │
        ▼
[Regresa a CartFragment (vacío)]
```

---

## 14. Sistema de colores

Los colores de los productos se manejan con **nombres en español** (no códigos HEX) para facilitar la gestión por parte del administrador.

El objeto `ColorConstants` es la fuente de verdad central:

```kotlin
object ColorConstants {
    data class ColorOption(val name: String, val hex: String)

    val ALL_COLORS = listOf(
        ColorOption("Blanco", "#FAFAFA"),
        ColorOption("Negro", "#212121"),
        ColorOption("Gris", "#9E9E9E"),
        ColorOption("Beige", "#D7CCC8"),
        ColorOption("Rojo", "#E53935"),
        ColorOption("Rosa", "#EC407A"),
        ColorOption("Morado", "#8E24AA"),
        ColorOption("Azul marino", "#1A237E"),
        ColorOption("Azul", "#1E88E5"),
        ColorOption("Celeste", "#29B6F6"),
        ColorOption("Verde", "#43A047"),
        ColorOption("Verde oscuro", "#2E7D32"),
        ColorOption("Amarillo", "#FDD835"),
        ColorOption("Naranja", "#FB8C00"),
        ColorOption("Marrón", "#6D4C41"),
        ColorOption("Dorado", "#FFD600"),
        ColorOption("Plateado", "#B0BEC5")
    )

    fun hexForName(name: String): String  // nombre → HEX (para renderizar el círculo)
    fun nameForHex(hex: String): String?  // HEX → nombre (para compatibilidad)
}
```

**En el admin:** se muestran chips con el círculo de color y el nombre para seleccionar cuáles colores están disponibles en el producto.  
**En el detalle del producto:** se muestran los colores como círculos con el nombre debajo.  
**En Firestore:** se almacena el nombre ("Rojo"), no el HEX.

---

## 15. Sistema de notificaciones

Las notificaciones son **internas de la aplicación** (no push notifications). Se almacenan en SharedPreferences usando Gson.

### Tipos de notificación
| Tipo | Ícono | Cuándo se genera |
|---|---|---|
| `ORDER` | 📦 | Al confirmar un pedido |
| `PROMO` | 🎉 | Manual o futura funcionalidad |
| `ECO` | 🌱 | Al completar retos ecológicos |
| `PRODUCT` | 🛍️ | Al agregar, editar o eliminar un producto (admin) |

### Navegación desde notificaciones
Al tocar una notificación, `NotificationsFragment` navega según `targetType`:
- `"product"` → `ProductDetailFragment` (si el producto aún existe)
- `"order"` → `OrdersFragment`
- `"category"` → `CategoryProductsFragment`
- Sin target → solo marca como leída

---

## 16. Panel de administrador

El panel de administrador es accesible únicamente para el usuario cuyo correo coincide con `ADMIN_EMAIL`.

### Gestión de productos (`AdminAddEditProductFragment`)
El formulario incluye:
- Nombre, descripción, categoría
- Precio, precio original, porcentaje de descuento
- URL de imagen
- Tallas disponibles (campo de texto separado por comas)
- Colores disponibles (ChipGroup visual con los 17 colores del sistema)
- Stock disponible, puntos eco, flags "Destacado" y "Nuevo"

Al guardar, el producto se sube/actualiza en Firestore y se genera automáticamente una notificación. El sistema detecta qué campos cambiaron (precio, colores, stock, descuento) para incluir un resumen en el mensaje de la notificación.

### Gestión de pedidos (`AdminOrdersFragment`)
El admin puede cambiar el estado de cualquier pedido entre: `PENDING → CONFIRMED → SHIPPED → DELIVERED` o `CANCELLED`.

---

## 17. Sistema de puntos ecológicos

Cada producto tiene un valor en `ecoPoints`. Al confirmar un pedido, se suman los puntos de todos los artículos comprados al perfil del usuario.

Los puntos se guardan en dos lugares:
- **Local:** `UserRepository` en SharedPreferences (acceso rápido)
- **Nube:** `FirestoreUserRepository` en el documento del usuario

### Retos ecológicos (`EcoRepository`)
Los retos se evalúan dinámicamente comparando:
- Total de puntos eco acumulados
- Cantidad de compras realizadas
- Categorías de productos comprados

Ejemplos de retos: "Primera compra ecológica", "Coleccionista verde", "Explorador de categorías", "100 puntos eco".

---

## 18. Sesión de usuario — AppSession

`AppSession` es un objeto singleton en memoria (no persistente) que almacena el estado de la sesión activa durante la ejecución de la app.

```kotlin
object AppSession {
    var userId: String    // UID de Firebase
    var userEmail: String
    var userRole: String  // "customer" o "admin"

    val isAdmin: Boolean get() = userRole == "admin"
    fun clear()          // Limpia todos los campos al cerrar sesión
}
```

Se inicializa en `SplashActivity` al detectar una sesión Firebase activa y se borra al hacer logout.

---

## 19. Configuración del proyecto

### Permisos requeridos (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Activities declaradas
| Activity | Exported | Tema | Rol |
|---|---|---|---|
| `SplashActivity` | true | Theme.ShopApp.Splash | Punto de entrada (launcher) |
| `AuthActivity` | false | Theme.ShopApp | Login y registro |
| `MainActivity` | false | Theme.ShopApp | Tienda principal |

### Archivo google-services.json
Requerido para la integración con Firebase. Debe colocarse en `app/`. Contiene la configuración del proyecto Firebase (API keys, IDs de proyecto). **No debe subirse a repositorios públicos.**

### Build
```
applicationId: com.shopapp
versionCode:   1
versionName:   1.0
minSdk:        24
targetSdk:     34
```

---

## 20. Decisiones técnicas importantes

### ¿Por qué Single Activity?
Facilita el manejo del estado compartido (el carrito, el usuario logueado, el badge del carrito) a través de un único ViewModel en el Activity. La navegación entre fragmentos es más fluida y el back stack es más predecible.

### ¿Por qué nombres de color en vez de HEX?
El administrador de la tienda no necesita conocer códigos HEX. Almacenar el nombre ("Rojo") en la base de datos es más legible, más fácil de mantener y más descriptivo para el usuario final. La conversión a HEX solo ocurre en la capa de UI para renderizar los círculos de color.

### ¿Por qué Firestore para direcciones y pagos y no solo SharedPreferences?
SharedPreferences es local al dispositivo. Si el usuario cambia de teléfono o inicia sesión en otro dispositivo, perdería todos sus datos. Al mover direcciones y métodos de pago a Firestore bajo el UID del usuario, los datos están disponibles desde cualquier dispositivo donde inicie sesión.

### Problema Gson + valores por defecto de Kotlin
Gson no respeta los valores por defecto de Kotlin al deserializar. Si un campo no existe en el JSON almacenado (por ejemplo, `targetType` en notificaciones antiguas), Gson lo setea como `null` en vez del valor por defecto. La solución es declarar esos campos como **nullable** (`String?`) y usar operadores null-safe (`isNullOrBlank()`, `?.let {}`).

### Bypass de Firestore para el administrador
El acceso del administrador no depende de que Firestore esté disponible. `SplashActivity` y `AuthViewModel` verifican el email localmente y asignan el rol "admin" directamente en `AppSession`, creando el documento en Firestore en segundo plano. Esto garantiza acceso incluso con conexión lenta o problemas de Firestore.

### setDefault con Batch Write
Al establecer una dirección o método de pago como predeterminado, se usa un **Firestore Batch Write** para actualizar todos los documentos en una única operación atómica. Esto evita inconsistencias donde momentáneamente dos registros aparecen como predeterminados.

---

*Documentación generada en Mayo 2026. Cualquier cambio posterior al proyecto debe reflejarse en este documento.*
