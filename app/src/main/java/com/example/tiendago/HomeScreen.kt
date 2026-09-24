package com.example.tiendago

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiendago.data.DashboardResumenResponse
import com.example.tiendago.data.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authToken: String,
    userName: String = "Administrador TiendaGo",
    userRole: String = "Administrador",
    onNuevaVentaClick: () -> Unit = {},
    onAgregarProductoClick: () -> Unit = {},
    onNavegarTab: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("Inicio") }
    var menuPerfilAbierto by remember { mutableStateOf(false) }

    var dashboardData by remember { mutableStateOf<DashboardResumenResponse?>(null) }
    var isLoadingMetrics by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Función para consultar las métricas reales del Dashboard desde el backend
    fun cargarDashboard() {
        if (authToken.isBlank()) {
            errorMessage = "No se ha proporcionado un token de sesión válido."
            return
        }

        coroutineScope.launch {
            isLoadingMetrics = true
            errorMessage = null
            try {
                val response = RetrofitClient.apiService.getDashboardResumen("Bearer $authToken")
                if (response.isSuccessful && response.body() != null) {
                    dashboardData = response.body()
                } else {
                    errorMessage = "Error al obtener métricas del servidor (HTTP ${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "No se pudo conectar con la API: ${e.localizedMessage ?: "Verifica la conexión a la red"}"
            } finally {
                isLoadingMetrics = false
            }
        }
    }

    // Cargar métricas al iniciar o si cambia el token
    LaunchedEffect(authToken) {
        cargarDashboard()
    }

    // Fecha actual formateada en español
    val fechaActual = remember {
        val localeEs = Locale.forLanguageTag("es-ES")
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", localeEs)
        sdf.format(Date()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    // Cálculos de métricas reales
    val totalVentas = dashboardData?.totalVentasDia ?: 0.0
    val totalVentasTexto = String.format(Locale.US, "$%,.2f", totalVentas)

    val ventasEfectivo = dashboardData?.ventasEfectivoDia ?: 0.0
    val ventasDigital = dashboardData?.ventasDigitalesDia ?: 0.0

    val cantidadOrdenes = dashboardData?.cantidadVentasDia ?: 0
    val cantidadCriticos = dashboardData?.totalProductosCriticos ?: 0

    // Meta diaria (ej. $2,000.00)
    val metaDiaria = 2000.0
    val progresoMeta = if (metaDiaria > 0) (totalVentas / metaDiaria).toFloat().coerceIn(0f, 1f) else 0f
    val porcentajeMeta = if (metaDiaria > 0) ((totalVentas / metaDiaria) * 100.0).coerceIn(0.0, 100.0) else 0.0

    Scaffold(
        containerColor = Color(0xFF0F172A),
        bottomBar = {
            // ==========================================
            // 5. BOTTOM NAVIGATION BAR
            // ==========================================
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    Triple("Inicio", Icons.Default.Home, "Inicio"),
                    Triple("Productos", Icons.Default.Inventory2, "Productos"),
                    Triple("Caja", Icons.Default.PointOfSale, "Caja"),
                    Triple("Historial", Icons.Default.History, "Historial")
                )

                items.forEach { (tabName, icon, label) ->
                    val isSelected = selectedTab == tabName
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedTab = tabName
                            when (tabName) {
                                "Caja" -> onNuevaVentaClick()
                                "Productos" -> onAgregarProductoClick()
                                else -> onNavegarTab(tabName)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF2563EB),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // ==========================================
                // 1. HEADER SUPERIOR
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo + Nombre TiendaGo
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Logo TiendaGo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "TiendaGo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Panel de Inicio",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Acciones Header: Refrescar, Búsqueda, Notificaciones, Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Botón de Recarga / Sync de Métricas
                        IconButton(onClick = { cargarDashboard() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recargar métricas",
                                tint = if (isLoadingMetrics) Color(0xFF38BDF8) else Color(0xFFCBD5E1)
                            )
                        }

                        // Campana con indicador
                        Box {
                            IconButton(onClick = { /* Notificaciones */ }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color(0xFFCBD5E1)
                                )
                            }
                            if (cantidadCriticos > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-8).dp, y = 8.dp)
                                )
                            }
                        }

                        // Avatar de Usuario con Menú de Sesión
                        Box {
                            IconButton(onClick = { menuPerfilAbierto = true }) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userName.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = menuPerfilAbierto,
                                onDismissRequest = { menuPerfilAbierto = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = userName,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Rol: $userRole",
                                                fontSize = 12.sp,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }
                                    },
                                    onClick = { },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8)
                                        )
                                    }
                                )
                                HorizontalDivider(color = Color(0xFF334155))
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Cerrar Sesión",
                                            color = Color(0xFFEF4444),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    onClick = {
                                        menuPerfilAbierto = false
                                        onLogoutClick()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Saludo Dinámico al Usuario con Badge de Rol
                Column {
                    Text(
                        text = "Hola, $userName 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge dinámico según el Rol autenticado
                        val esAdmin = userRole.equals("Administrador", ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (esAdmin) Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (esAdmin) Color(0xFF38BDF8) else Color(0xFF34D399))
                        ) {
                            Text(
                                text = userRole.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (esAdmin) Color(0xFF38BDF8) else Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "•  $fechaActual",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // ==========================================
            // ESTADO DE CARGA / ERROR DE MÉTRICAS
            // ==========================================
            if (isLoadingMetrics) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF38BDF8),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sincronizando métricas en tiempo real con Supabase...",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFFCA5A5)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFFEE2E2),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            TextButton(onClick = { cargarDashboard() }) {
                                Text(
                                    text = "Reintentar",
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. ACCIONES RÁPIDAS (BOTONES PRINCIPALES)
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón destacado: + Nueva Venta (Navega a Scanner/POS)
                    Button(
                        onClick = onNuevaVentaClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ Nueva Venta",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Botón secundario: + Producto
                    OutlinedButton(
                        onClick = onAgregarProductoClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF1E293B)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF475569), Color(0xFF334155))
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Producto",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // ==========================================
            // 3. MÉTRICAS EN TIEMPO REAL (OVERVIEW REAL)
            // ==========================================
            item {
                Text(
                    text = "Métricas en Tiempo Real",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Tarjeta Principal: "Ventas de Hoy" (Datos Reales)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF38BDF8))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VENTAS DE HOY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )

                            // Indicador Porcentual calculado según ventas registradas
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (totalVentas > 0) Color(0xFF10B981).copy(alpha = 0.18f) else Color(0xFF334155)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = if (totalVentas > 0) Color(0xFF34D399) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (totalVentas > 0) String.format(Locale.US, "+%.1f%% meta", porcentajeMeta) else "0.0% hoy",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalVentas > 0) Color(0xFF34D399) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Monto Destacado Real
                        Text(
                            text = totalVentasTexto,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        // Desglose Efectivo vs Digital
                        Text(
                            text = "Efectivo: ${String.format(Locale.US, "$%,.2f", ventasEfectivo)} · Digital: ${String.format(Locale.US, "$%,.2f", ventasDigital)}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Barra de Progreso Dinámica calculada según la Meta Diaria
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Meta diaria: $2,000.00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", porcentajeMeta),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (progresoMeta > 0) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progresoMeta },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            // Dos Tarjetas en Fila: "Órdenes Totales" y "Stock Crítico"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tarjeta 1: Órdenes Totales
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Órdenes Totales",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )

                            Text(
                                text = "$cantidadOrdenes órdenes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = if (cantidadOrdenes == 0) "Sin órdenes hoy" else "$cantidadOrdenes procesadas",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tarjeta 2: Stock Crítico
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(8.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val tieneAlerta = cantidadCriticos > 0
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (tieneAlerta) Color(0xFFEF4444).copy(alpha = 0.2f)
                                            else Color(0xFF10B981).copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tieneAlerta) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (tieneAlerta) Color(0xFFF87171) else Color(0xFF34D399),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (tieneAlerta) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (tieneAlerta) "Alerta" else "Óptimo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tieneAlerta) Color(0xFFF87171) else Color(0xFF34D399),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Stock Crítico",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )

                            Text(
                                text = "$cantidadCriticos ítems",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = if (cantidadCriticos > 0) "Por agotar (< 5 uds)" else "Inventario seguro",
                                fontSize = 11.sp,
                                color = if (cantidadCriticos > 0) Color(0xFFF87171) else Color(0xFF34D399),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 4. REGISTRO DE ACTIVIDAD RECIENTE
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividad Reciente",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0)
                    )

                    Text(
                        text = "Ver todo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.clickable { /* Ver historial completo */ }
                    )
                }
            }

            // Estado cuando no hay transacciones registradas en el día
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF334155), Color(0xFF1E293B))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155).copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Sin actividad reciente registrada hoy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Las transacciones y ventas procesadas en la terminal POS aparecerán aquí en tiempo real.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onNuevaVentaClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF2563EB)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF2563EB).copy(alpha = 0.1f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Iniciar Primera Venta",
                                fontSize = 13.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Si hay productos con stock crítico reportados por el backend, mostrarlos como alerta
            if (!dashboardData?.productosStockBajo.isNullOrEmpty()) {
                item {
                    Text(
                        text = "Alertas de Inventario (${dashboardData!!.productosStockBajo.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF87171),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(dashboardData!!.productosStockBajo.take(3).size) { index ->
                    val prod = dashboardData!!.productosStockBajo[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = Color(0xFFF87171),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = prod.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = prod.nombreCategoria ?: "Sin categoría",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${prod.stock} disponibles",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF87171),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
