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
import java.util.concurrent.TimeUnit

// --- Estado global de Sesión en memoria ---
object UserSession {
    var token: String? = null
    var nombre: String = "Administrador TiendaGo"
    var rol: String = "Administrador"
    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    fun actualizar(nuevoToken: String?, nuevoNombre: String?, nuevoRol: String?) {
        token = nuevoToken
        nombre = nuevoNombre?.ifBlank { null } ?: "Administrador TiendaGo"
        rol = nuevoRol?.ifBlank { null } ?: "Administrador"
    }

    fun clear() {
        token = null
        nombre = "Administrador TiendaGo"
        rol = "Administrador"
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
    val idProducto: Int,

    @SerializedName("codigoBarra")
    val codigoBarra: String? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("stock")
    val stock: Int,

    @SerializedName("idCategoria")
    val idCategoria: Int = 0,

    @SerializedName("nombreCategoria")
    val nombreCategoria: String? = null,

    @SerializedName("estado")
    val estado: Boolean = true
)

// --- Endpoints de la API ---
interface TiendaGoApiService {

    @GET("api/ping")
    suspend fun ping(): Response<Map<String, Any>>

    // Endpoint para iniciar sesión
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Endpoint del Dashboard consolidado
    @GET("api/dashboard/resumen")
    suspend fun getDashboardResumen(@Header("Authorization") authHeader: String): Response<DashboardResumenResponse>

    // Endpoint para búsqueda y escaneo de productos
    @GET("api/productos/buscar/{codigo}")
    suspend fun buscarPorCodigo(@Path("codigo") codigo: String): Response<ProductoResponse>
}

// --- Cliente Retrofit Singleton ---
object RetrofitClient {
    // 10.0.2.2 apunta al localhost de la máquina host desde el emulador de Android Studio
    // Para dispositivo físico en la misma red Wi-Fi usar: "http://192.168.0.3:5000/"
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
