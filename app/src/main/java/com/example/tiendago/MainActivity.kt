package com.example.tiendago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tiendago.data.UserSession
import com.example.tiendago.ui.historial.HistorialVentasScreen
import com.example.tiendago.ui.inventario.InventarioScreen
import com.example.tiendago.ui.inventario.NuevoProductoScreen
import com.example.tiendago.ui.pos.CobroRapidoScreen
import com.example.tiendago.ui.pos.ConfirmarPagoScreen
import com.example.tiendago.ui.theme.TiendaGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiendaGoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    var authToken by remember { mutableStateOf<String?>(UserSession.token) }
                    var currentUserName by remember { mutableStateOf(UserSession.nombre) }
                    var currentUserRole by remember { mutableStateOf(UserSession.rol) }

                    fun navegarATab(tabName: String) {
                        when (tabName) {
                            "Inicio" -> navController.navigate("dashboard") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            "Productos" -> navController.navigate("inventario") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            "Caja" -> navController.navigate("pos") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            "Historial" -> navController.navigate("historial") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = if (UserSession.isLoggedIn) "dashboard" else "login"
                    ) {
                        // 1. Pantalla de Login (W-01)
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { token, nombre, rol ->
                                    authToken = token
                                    currentUserName = nombre
                                    currentUserRole = rol
                                    UserSession.actualizar(token, nombre, rol)

                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Pantalla de Dashboard / Home (W-02 con datos reales)
                        composable("dashboard") {
                            HomeScreen(
                                authToken = authToken ?: UserSession.token ?: "",
                                userName = currentUserName,
                                userRole = currentUserRole,
                                onNuevaVentaClick = {
                                    navController.navigate("pos")
                                },
                                onAgregarProductoClick = {
                                    navController.navigate("nuevo_producto")
                                },
                                onNavegarTab = { tab ->
                                    navegarATab(tab)
                                },
                                onLogoutClick = {
                                    authToken = null
                                    UserSession.clear()
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Inventario / Catálogo (W-03)
                        composable("inventario") {
                            InventarioScreen(
                                onNavigateToNuevoProducto = {
                                    navController.navigate("nuevo_producto")
                                },
                                onNavigateToScanner = {
                                    navController.navigate("scanner")
                                },
                                onTabSelected = { tab ->
                                    navegarATab(tab)
                                }
                            )
                        }

                        // 4. Formulario Nuevo Producto (W-04)
                        composable("nuevo_producto") {
                            NuevoProductoScreen(
                                onDescartar = {
                                    navController.popBackStack()
                                },
                                onProductoGuardado = {
                                    navController.popBackStack()
                                },
                                onNavigateToScanner = {
                                    navController.navigate("scanner")
                                }
                            )
                        }

                        // 5. Terminal POS / Cobro Rápido (W-05)
                        composable("pos") {
                            CobroRapidoScreen(
                                onNavigateToConfirmarPago = {
                                    navController.navigate("confirmar_pago")
                                },
                                onNavigateToScanner = {
                                    navController.navigate("scanner")
                                },
                                onTabSelected = { tab ->
                                    navegarATab(tab)
                                }
                            )
                        }

                        // 6. Confirmar Pago (W-06)
                        composable("confirmar_pago") {
                            ConfirmarPagoScreen(
                                onCancelar = {
                                    navController.popBackStack()
                                },
                                onVentaExitosa = {
                                    navController.navigate("pos") {
                                        popUpTo("pos") { inclusive = true }
                                    }
                                },
                                onIrAHistorial = {
                                    navController.navigate("historial") {
                                        popUpTo("pos") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 7. Historial de Ventas (W-07)
                        composable("historial") {
                            HistorialVentasScreen(
                                onTabSelected = { tab ->
                                    navegarATab(tab)
                                }
                            )
                        }

                        // 8. Pantalla de Scanner ML Kit
                        composable("scanner") {
                            Box(modifier = Modifier.fillMaxSize()) {
                                ScannerScreen()

                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .padding(16.dp)
                                        .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Regresar",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
