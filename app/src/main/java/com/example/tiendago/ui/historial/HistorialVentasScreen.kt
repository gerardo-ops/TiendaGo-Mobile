package com.example.tiendago.ui.historial

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiendago.data.DetalleVentaResponse
import com.example.tiendago.data.RetrofitClient
import com.example.tiendago.data.UserSession
import com.example.tiendago.data.VentaResponse
import com.example.tiendago.ui.components.TiendaGoBottomBar
import com.example.tiendago.ui.components.TiendaGoColors
import com.example.tiendago.ui.components.TiendaGoTopBar
import com.example.tiendago.ui.components.formatPrice
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialVentasScreen(
    onTabSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFiltro by remember { mutableStateOf("Hoy") }
    val filtros = listOf("Hoy", "Ayer", "Esta Semana", "ðŸ“… Rango")

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var ticketsList by remember { mutableStateOf<List<VentaResponse>>(emptyList()) }
    var selectedTicketParaDetalle by remember { mutableStateOf<VentaResponse?>(null) }

    val horaActual = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    // Mock realista de respaldo (Wireframe 5)
    val mockTickets = remember {
        listOf(
            VentaResponse(
                idVenta = 8942,
                numeroTicket = "ORD-8942",
                fechaHora = "18:42",
                subtotal = 6.40,
                totalVenta = 6.40,
                montoRecibido = 10.00,
                cambioEntregado = 3.60,
                idMetodoPago = 1,
                nombreMetodoPago = "Efectivo",
                detalles = listOf(
                    DetalleVentaResponse(1, 8942, 1, "Coca-Cola 1.5L", "BEB-01", 1, 2.50, 2.50),
                    DetalleVentaResponse(2, 8942, 2, "Galletas Oreo Pack", "SNK-05", 1, 1.90, 1.90),
                    DetalleVentaResponse(3, 8942, 3, "Doritos Mega Queso", "SNK-12", 1, 2.00, 2.00)
                )
            ),
            VentaResponse(
                idVenta = 8941,
                numeroTicket = "ORD-8941",
                fechaHora = "17:15",
                subtotal = 15.80,
                totalVenta = 15.80,
                montoRecibido = 15.80,
                cambioEntregado = 0.00,
                idMetodoPago = 2,
                nombreMetodoPago = "QR Transferencia",
                detalles = listOf(
                    DetalleVentaResponse(4, 8941, 4, "Leche Entera 1L", "LAC-01", 2, 1.40, 2.80),
                    DetalleVentaResponse(5, 8941, 5, "Huevos 12pk", "ABA-09", 1, 3.50, 3.50),
                    DetalleVentaResponse(6, 8941, 6, "Arroz Superior 5kg", "ABA-01", 1, 9.50, 9.50)
                )
            ),
            VentaResponse(
                idVenta = 8940,
                numeroTicket = "ORD-8940",
                fechaHora = "16:05",
                subtotal = 3.50,
                totalVenta = 3.50,
                montoRecibido = 3.50,
                cambioEntregado = 0.00,
                idMetodoPago = 2,
                nombreMetodoPago = "Tarjeta DÃ©bito",
                detalles = listOf(
                    DetalleVentaResponse(7, 8940, 7, "Recarga MÃ³vil 5GB Operador", "SRV-01", 1, 3.50, 3.50)
                )
            ),
            VentaResponse(
                idVenta = 8939,
                numeroTicket = "ORD-8939",
                fechaHora = "14:28",
                subtotal = 42.00,
                totalVenta = 42.00,
                montoRecibido = 42.00,
                cambioEntregado = 0.00,
                idMetodoPago = 1,
                nombreMetodoPago = "Efectivo",
                detalles = listOf(
                    DetalleVentaResponse(8, 8939, 8, "Pack Cerveza Especial", "BEB-10", 2, 14.00, 28.00),
                    DetalleVentaResponse(9, 8939, 9, "Hielo 3kg", "REF-02", 2, 2.50, 5.00),
                    DetalleVentaResponse(10, 8939, 10, "Snacks Variados Fiesta", "SNK-20", 3, 3.00, 9.00)
                )
            ),
            VentaResponse(
                idVenta = 8938,
                numeroTicket = "ORD-8938",
                fechaHora = "12:10",
                subtotal = 8.20,
                totalVenta = 8.20,
                montoRecibido = 10.00,
                cambioEntregado = 1.80,
                idMetodoPago = 1,
                nombreMetodoPago = "Efectivo",
                detalles = listOf(
                    DetalleVentaResponse(11, 8938, 11, "Pan Baguette x2", "PAN-01", 2, 1.50, 3.00),
                    DetalleVentaResponse(12, 8938, 12, "Queso Crema 200g", "LAC-03", 1, 2.70, 2.70),
                    DetalleVentaResponse(13, 8938, 13, "Jugo Naranja 1L", "BEB-04", 1, 2.50, 2.50)
                )
            )
        )
    }

    fun cargarHistorial(filtro: String) {
        coroutineScope.launch {
            isLoading = true
            try {
                val filtroParam = when (filtro) {
                    "Hoy" -> "Hoy"
                    "Ayer" -> "Ayer"
                    "Esta Semana" -> "Semana"
                    else -> "Hoy"
                }

                val response = RetrofitClient.apiService.getHistorialVentas(
                    authHeader = UserSession.authHeader(),
                    filtroFecha = filtroParam
                )

                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    ticketsList = response.body()!!
                } else {
                    ticketsList = mockTickets
                }
            } catch (e: Exception) {
                ticketsList = mockTickets
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedFiltro) {
        cargarHistorial(selectedFiltro)
    }

    // MÃ©tricas calculadas para la tarjeta de resumen de turno (Wireframe 5)
    val totalFacturado = remember(ticketsList) { ticketsList.sumOf { it.totalVenta } }
    val totalEfectivo = remember(ticketsList) {
        ticketsList.filter { it.esEfectivo }.sumOf { it.totalVenta }
    }
    val totalQr = remember(ticketsList) {
        ticketsList.filter { !it.esEfectivo }.sumOf { it.totalVenta }
    }

    // Filtrado por buscador
    val filteredTickets = remember(ticketsList, searchQuery) {
        if (searchQuery.isBlank()) {
            ticketsList
        } else {
            ticketsList.filter { ticket ->
                ticket.numeroTicket.contains(searchQuery, ignoreCase = true) ||
                ticket.displayMetodoPago.contains(searchQuery, ignoreCase = true) ||
                ticket.detalles.any { it.nombreProducto?.contains(searchQuery, ignoreCase = true) == true } ||
                ticket.totalVenta.toString().contains(searchQuery)
            }
        }
    }

    Scaffold(
        topBar = {
            TiendaGoTopBar(
                screenTitle = "Historial de Ventas",
                tagSubtitle = "[TIENDAGO]",
                onSearchClick = {},
                onProfileClick = {}
            )
        },
        bottomBar = {
            TiendaGoBottomBar(
                currentTab = "Historial",
                onTabSelected = onTabSelected
            )
        },
        containerColor = TiendaGoColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header del Historial (Wireframe 5)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "[VENTAS_HIST] Caja Principal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de Ventas",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TiendaGoColors.TextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { cargarHistorial(selectedFiltro) },
                                color = Color.White,
                                border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filtros",
                                        tint = TiendaGoColors.DarkSlate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        Toast.makeText(context, "Imprimiendo reporte del turno...", Toast.LENGTH_SHORT).show()
                                    },
                                color = Color.White,
                                border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = "Imprimir Reporte",
                                        tint = TiendaGoColors.DarkSlate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // PestaÃ±as de filtro de fecha (Wireframe 5)
            item {
                LazyRow(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filtros) { filtro ->
                        val isSelected = filtro == selectedFiltro
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedFiltro = filtro },
                            color = if (isSelected) TiendaGoColors.DarkSlate else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = filtro,
                                color = if (isSelected) Color.White else TiendaGoColors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // TARJETA: RESUMEN DE TURNO (Wireframe 5)
            // ==========================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(TiendaGoColors.PrimaryBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RESUMEN DE TURNO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TiendaGoColors.TextSecondary
                                )
                            }

                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${ticketsList.size} TICKETS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatPrice(totalFacturado),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TiendaGoColors.DarkSlate
                                )
                                Text(
                                    text = "Total facturado visible",
                                    fontSize = 11.sp,
                                    color = TiendaGoColors.TextMuted
                                )
                            }

                            // Desglose Efectivo vs QR (Wireframe 5)
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = TiendaGoColors.SuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${formatPrice(totalEfectivo)} efec.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TiendaGoColors.TextPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = TiendaGoColors.PrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${formatPrice(totalQr)} QR/transf.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TiendaGoColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Buscador de Tickets
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar ticket, cliente o monto...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TiendaGoColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = TiendaGoColors.TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TiendaGoColors.PrimaryBlue,
                        unfocusedBorderColor = TiendaGoColors.BorderLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Subheader de Tickets
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TICKETS RECIENTES (${selectedFiltro.uppercase()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextSecondary
                    )
                    Text(
                        text = "ACTUALIZADO $horaActual",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TiendaGoColors.TextMuted
                    )
                }
            }

            // ==========================================
            // LISTA DE TICKETS (Wireframe 5)
            // ==========================================
            if (filteredTickets.isEmpty() && !isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TiendaGoColors.TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No hay tickets registrados",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextSecondary
                        )
                        Text(
                            text = "Las transacciones de este turno aparecerÃ¡n aquÃ­.",
                            fontSize = 12.sp,
                            color = TiendaGoColors.TextMuted
                        )
                    }
                }
            } else {
                items(filteredTickets, key = { it.idVenta }) { ticket ->
                    TicketCard(
                        ticket = ticket,
                        onClick = { selectedTicketParaDetalle = ticket }
                    )
                }
            }
        }
    }

    // DiÃ¡logo Detallado de Ticket (Modal Desglose Wireframe 5)
    selectedTicketParaDetalle?.let { ticket ->
        AlertDialog(
            onDismissRequest = { selectedTicketParaDetalle = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ticket #${ticket.numeroTicket}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TiendaGoColors.DarkSlate
                        )
                        Text(
                            text = "Hora: ${ticket.fechaHora} â€¢ Turno #${ticket.idTurno}",
                            fontSize = 11.sp,
                            color = TiendaGoColors.TextMuted
                        )
                    }
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "COMPLETADO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.SuccessGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "ArtÃ­culos Facturados:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ticket.detalles.forEach { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${d.cantidad}x ${d.nombreProducto ?: "Producto"}",
                                fontSize = 12.sp,
                                color = TiendaGoColors.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatPrice(d.subtotalLinea),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextPrimary
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Venta:", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = formatPrice(ticket.totalVenta), fontSize = 16.sp, fontWeight = FontWeight.Black, color = TiendaGoColors.PrimaryBlue)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "MÃ©todo:", fontSize = 12.sp, color = TiendaGoColors.TextSecondary)
                        Text(text = ticket.displayMetodoPago, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (ticket.esEfectivo && ticket.montoRecibido > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "PagÃ³ con:", fontSize = 12.sp, color = TiendaGoColors.TextSecondary)
                            Text(text = formatPrice(ticket.montoRecibido), fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Cambio:", fontSize = 12.sp, color = TiendaGoColors.SuccessGreen, fontWeight = FontWeight.Bold)
                            Text(text = formatPrice(ticket.cambioEntregado), fontSize = 12.sp, color = TiendaGoColors.SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Ticket #${ticket.numeroTicket} enviado a imprimir", Toast.LENGTH_SHORT).show()
                        selectedTicketParaDetalle = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TiendaGoColors.DarkSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reimprimir Ticket")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedTicketParaDetalle = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun TicketCard(
    ticket: VentaResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Fila 1: #ORD-XXXX  Hora  ----  Total >
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${ticket.numeroTicket}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.DarkSlate
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = ticket.fechaHora,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TiendaGoColors.TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatPrice(ticket.totalVenta),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TiendaGoColors.DarkSlate
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detalle",
                        tint = TiendaGoColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fila 2: ArtÃ­culos resumen
            val itemsPreview = ticket.detalles.joinToString(", ") { it.nombreProducto ?: "Ãtem" }
            val countItems = if (ticket.detalles.isNotEmpty()) ticket.detalles.sumOf { it.cantidad } else 1

            Text(
                text = "$countItems arts. ($itemsPreview)",
                fontSize = 12.sp,
                color = TiendaGoColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Fila 3: MÃ©todo de pago y detalles de cambio
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ticket.esEfectivo) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Efectivo",
                        tint = TiendaGoColors.SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (ticket.montoRecibido > 0.0 && ticket.cambioEntregado > 0.0) {
                            "Efectivo (PagÃ³ ${formatPrice(ticket.montoRecibido)} Â· Cambio ${formatPrice(ticket.cambioEntregado)})"
                        } else {
                            "Efectivo Exacto"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TiendaGoColors.TextSecondary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "QR Transferencia",
                        tint = TiendaGoColors.PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticket.displayMetodoPago,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TiendaGoColors.PrimaryBlue
                    )
                }
            }
        }
    }
}
