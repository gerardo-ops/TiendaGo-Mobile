package com.example.tiendago.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Estado global de Sesión en memoria ---
object UserSession {
    var token: String? = null
    var nombre: String = "Administrador TiendaGo"
    var rol: String = "Administrador"
    var idUsuarioGuid: String = "00000000-0000-0000-0000-000000000001"
    var idTurnoActual: Long = 1L
    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    fun authHeader(): String {
        return if (!token.isNullOrBlank()) "Bearer $token" else ""
    }

    fun actualizar(
        nuevoToken: String?,
        nuevoNombre: String?,
        nuevoRol: String?,
        nuevoGuid: String? = null,
        nuevoTurno: Long = 1L
    ) {
        token = nuevoToken
        nombre = nuevoNombre?.ifBlank { null } ?: "Administrador TiendaGo"
        rol = nuevoRol?.ifBlank { null } ?: "Administrador"
        if (!nuevoGuid.isNullOrBlank()) {
            idUsuarioGuid = nuevoGuid
        }
        idTurnoActual = nuevoTurno
    }

    fun clear() {
        token = null
        nombre = "Administrador TiendaGo"
        rol = "Administrador"
        idUsuarioGuid = "00000000-0000-0000-0000-000000000001"
        idTurnoActual = 1L
    }
}

// --- DTOs sincronizados con el backend .NET ---
data class LoginRequest(
    @SerializedName("usuarioOCorreo")
    val usuarioOCorreo: String? = null,

    @SerializedName("password")
    val password: String? = null,

    @SerializedName("correoElectronico")
    val correoElectronico: String? = usuarioOCorreo,

    @SerializedName("clave")
    val clave: String? = password
)

data class LoginResponse(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("idUsuario")
    val idUsuario: Any? = null,

    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("nombreCompleto")
    val nombreCompleto: String? = null,

    @SerializedName("correo")
    val correo: String? = null,

    @SerializedName("rol")
    val rol: String? = null,

    @SerializedName("expiracion")
    val expiracion: String? = null,

    @SerializedName("idUsuarioGuid")
    val idUsuarioGuid: String? = null,

    @SerializedName("mensaje")
    val mensaje: String? = null
)

data class DashboardResumenResponse(
    @SerializedName("totalVentasDia")
    val totalVentasDia: Double = 0.0,

    @SerializedName("cantidadVentasDia")
    val cantidadVentasDia: Int = 0,

    @SerializedName("ventasEfectivoDia")
    val ventasEfectivoDia: Double = 0.0,

    @SerializedName("ventasDigitalesDia")
    val ventasDigitalesDia: Double = 0.0,

    @SerializedName("totalProductosCriticos")
    val totalProductosCriticos: Int = 0,

    @SerializedName("productosStockBajo")
    val productosStockBajo: List<ProductoResponse> = emptyList()
)

data class ProductoResponse(
    @SerializedName("idProducto")
    val idProducto: Int = 0,

    @SerializedName("codigoBarra")
    val codigoBarra: String? = null,

    @SerializedName("codigoSku")
    val codigoSku: String? = null,

    @SerializedName("nombre")
    val nombre: String = "",

    @SerializedName("nombreProducto")
    val nombreProducto: String? = null,

    @SerializedName("precio")
    val precio: Double = 0.0,

    @SerializedName("precioVenta")
    val precioVenta: Double? = null,

    @SerializedName("costoCompra")
    val costoCompra: Double = 0.0,

    @SerializedName("stock")
    val stock: Int = 0,

    @SerializedName("stockActual")
    val stockActual: Int? = null,

    @SerializedName("stockMinimo")
    val stockMinimo: Int = 5,

    @SerializedName("idCategoria")
    val idCategoria: Int = 1,

    @SerializedName("nombreCategoria")
    val nombreCategoria: String? = null,

    @SerializedName("urlImagen")
    val urlImagen: String? = null,

    @SerializedName("estado")
    val estado: Boolean = true
) {
    val displayNombre: String
        get() = nombre.ifBlank { nombreProducto ?: "Producto sin nombre" }

    val displayCodigo: String
        get() = codigoBarra?.ifBlank { null }
            ?: codigoSku?.ifBlank { null }
            ?: "SKU-${idProducto.toString().padStart(4, '0')}"

    val displayPrecio: Double
        get() = if (precio > 0.0) precio else (precioVenta ?: 0.0)

    val displayStock: Int
        get() = stockActual ?: stock

    val esCritico: Boolean
        get() = displayStock < 5 || displayStock <= stockMinimo

    val displayCategoria: String
        get() = nombreCategoria?.ifBlank { null } ?: when (idCategoria) {
            1 -> "Bebidas"
            2 -> "Snacks"
            3 -> "Abarrotes"
            4 -> "Lácteos"
            5 -> "Limpieza"
            6 -> "Panadería"
            else -> "General"
        }
}

data class ProductoRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("nombreProducto")
    val nombreProducto: String = nombre,

    @SerializedName("codigoBarra")
    val codigoBarra: String,

    @SerializedName("codigoSku")
    val codigoSku: String = codigoBarra,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("precioVenta")
    val precioVenta: Double = precio,

    @SerializedName("costoCompra")
    val costoCompra: Double = 0.0,

    @SerializedName("stock")
    val stock: Int = 0,

    @SerializedName("stockActual")
    val stockActual: Int = stock,

    @SerializedName("stockMinimo")
    val stockMinimo: Int = 5,

    @SerializedName("idCategoria")
    val idCategoria: Int = 1,

    @SerializedName("urlImagen")
    val urlImagen: String? = null,

    @SerializedName("estado")
    val estado: Boolean = true
)

data class DetalleVentaRequest(
    @SerializedName("idProducto")
    val idProducto: Long,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("precioUnitarioHistorico")
    val precioUnitarioHistorico: Double? = null
)

data class VentaRequest(
    @SerializedName("idTurno")
    val idTurno: Long = 1,

    @SerializedName("idUsuario")
    val idUsuario: String = "00000000-0000-0000-0000-000000000001",

    @SerializedName("idMetodoPago")
    val idMetodoPago: Long = 1, // 1: Efectivo, 2: QR / Transferencia

    @SerializedName("montoRecibido")
    val montoRecibido: Double = 0.0,

    @SerializedName("cambioEntregado")
    val cambioEntregado: Double = 0.0,

    @SerializedName("detalles")
    val detalles: List<DetalleVentaRequest> = emptyList()
)

data class DetalleVentaResponse(
    @SerializedName("idDetalle")
    val idDetalle: Long = 0,

    @SerializedName("idVenta")
    val idVenta: Long = 0,

    @SerializedName("idProducto")
    val idProducto: Long = 0,

    @SerializedName("nombreProducto")
    val nombreProducto: String? = null,

    @SerializedName("codigoSku")
    val codigoSku: String? = null,

    @SerializedName("cantidad")
    val cantidad: Int = 0,

    @SerializedName("precioUnitarioHistorico")
    val precioUnitarioHistorico: Double = 0.0,

    @SerializedName("subtotalLinea")
    val subtotalLinea: Double = 0.0
)

data class VentaResponse(
    @SerializedName("idVenta")
    val idVenta: Long = 0,

    @SerializedName("idTurno")
    val idTurno: Long = 1,

    @SerializedName("idUsuario")
    val idUsuario: String? = null,

    @SerializedName("nombreUsuario")
    val nombreUsuario: String? = null,

    @SerializedName("idMetodoPago")
    val idMetodoPago: Long = 1,

    @SerializedName("nombreMetodoPago")
    val nombreMetodoPago: String? = null,

    @SerializedName("numeroTicket")
    val numeroTicket: String = "",

    @SerializedName("fechaHora")
    val fechaHora: String = "",

    @SerializedName("subtotal")
    val subtotal: Double = 0.0,

    @SerializedName("totalIva")
    val totalIva: Double = 0.0,

    @SerializedName("totalVenta")
    val totalVenta: Double = 0.0,

    @SerializedName("montoRecibido")
    val montoRecibido: Double = 0.0,

    @SerializedName("cambioEntregado")
    val cambioEntregado: Double = 0.0,

    @SerializedName("estadoVenta")
    val estadoVenta: String = "Completada",

    @SerializedName("detalles")
    val detalles: List<DetalleVentaResponse> = emptyList()
) {
    val esEfectivo: Boolean
        get() = idMetodoPago == 1L || nombreMetodoPago?.contains("Efectivo", ignoreCase = true) == true

    val displayMetodoPago: String
        get() = if (esEfectivo) "Efectivo" else "QR Transferencia"
}

// --- Endpoints de la API ---
interface TiendaGoApiService {

    @GET("api/ping")
    suspend fun ping(): Response<Map<String, Any>>

    // Endpoint para iniciar sesión
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Endpoint del Dashboard consolidado
    @GET("api/dashboard/resumen")
    suspend fun getDashboardResumen(
        @Header("Authorization") authHeader: String
    ): Response<DashboardResumenResponse>

    // --- Catálogo e Inventario ---
    @GET("api/productos")
    suspend fun getProductos(
        @Query("buscar") buscar: String? = null,
        @Query("categoria") categoria: Long? = null
    ): Response<List<ProductoResponse>>

    @GET("api/productos/activos")
    suspend fun getProductosActivos(): Response<List<ProductoResponse>>

    @GET("api/productos/buscar/{codigo}")
    suspend fun buscarPorCodigo(
        @Path("codigo") codigo: String
    ): Response<ProductoResponse>

    @POST("api/productos")
    suspend fun crearProducto(
        @Header("Authorization") authHeader: String,
        @Body request: ProductoRequest
    ): Response<ProductoResponse>

    // --- Ventas POS ---
    @POST("api/ventas")
    suspend fun registrarVenta(
        @Header("Authorization") authHeader: String,
        @Body request: VentaRequest
    ): Response<VentaResponse>

    // --- Historial de Ventas ---
    @GET("api/ventas/historial")
    suspend fun getHistorialVentas(
        @Header("Authorization") authHeader: String,
        @Query("filtroFecha") filtroFecha: String? = null
    ): Response<List<VentaResponse>>

    @GET("api/ventas")
    suspend fun getVentas(
        @Header("Authorization") authHeader: String
    ): Response<List<VentaResponse>>

    @GET("api/ventas/turno/{idTurno}")
    suspend fun getVentasPorTurno(
        @Header("Authorization") authHeader: String,
        @Path("idTurno") idTurno: Long
    ): Response<List<VentaResponse>>
}

// --- Cliente Retrofit Singleton ---
object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.3:5000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: TiendaGoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TiendaGoApiService::class.java)
    }
}
