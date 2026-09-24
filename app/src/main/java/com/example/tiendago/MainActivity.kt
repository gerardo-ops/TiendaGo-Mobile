package com.example.tiendago

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tiendago.data.UserSession
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
                    val context = LocalContext.current

                    var authToken by remember { mutableStateOf<String?>(UserSession.token) }
                    var currentUserName by remember { mutableStateOf(UserSession.nombre) }
                    var currentUserRole by remember { mutableStateOf(UserSession.rol) }

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

                                    // Navegación limpia al dashboard, removiendo login del backstack
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
                                    navController.navigate("scanner")
                                },
                                onAgregarProductoClick = {
                                    Toast.makeText(
                                        context,
                                        "Acceso a Formulario de Producto (W-04)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onNavegarTab = { tab ->
                                    if (tab != "Inicio") {
                                        Toast.makeText(
                                            context,
                                            "Módulo '$tab' seleccionado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
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

                        // 3. Pantalla de Scanner / Terminal POS (W-05)
                        composable("scanner") {
                            Box(modifier = Modifier.fillMaxSize()) {
                                ScannerScreen()

                                // Botón flotante para regresar al dashboard
                                IconButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .padding(16.dp)
                                        .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Regresar al Dashboard",
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
