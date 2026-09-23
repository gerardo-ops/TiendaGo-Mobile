package com.example.tiendago.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- DTOs que coinciden con tu backend .NET ---
data class LoginRequest(
    val usuarioOCorreo: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val idUsuario: Int,
    val nombre: String,
    val rol: String
)

data class ProductoResponse(
    val idProducto: Int,
    val codigoBarra: String?,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val idCategoria: Int,
    val nombreCategoria: String?,
    val estado: Boolean
)

// --- Endpoints de la API ---
interface TiendaGoApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/productos")
    suspend fun obtenerProductos(): Response<List<ProductoResponse>>

    // Endpoint de prueba de conexión / Healthcheck
    @GET("api/ping")
    suspend fun ping(): Response<Map<String, Any>>

    // Endpoint directo para el escaneo de cámara
    @GET("api/productos/buscar/{codigo}")
    suspend fun buscarPorCodigo(@Path("codigo") codigo: String): Response<ProductoResponse>
}

// --- Cliente Retrofit Singleton ---
object RetrofitClient {
    // Si usas el emulador de Android Studio: "http://10.0.2.2:5000/" (ajusta a tu puerto)
    // Si usas telefono fisico: "http://TU_IP_LOCAL:PUERTO/"
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val apiService: TiendaGoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TiendaGoApiService::class.java)
    }
}