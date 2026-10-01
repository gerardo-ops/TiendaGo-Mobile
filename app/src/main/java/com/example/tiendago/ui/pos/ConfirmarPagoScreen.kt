package com.example.tiendago.ui.pos

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiendago.data.DetalleVentaRequest
import com.example.tiendago.data.RetrofitClient
import com.example.tiendago.data.UserSession
import com.example.tiendago.data.VentaRequest
import com.example.tiendago.data.VentaResponse
import com.example.tiendago.ui.components.TiendaGoColors
import com.example.tiendago.ui.components.formatPrice
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.ceil

@Composable
fun ConfirmarPagoScreen(
    onCancelar: () -> Unit,
    onVentaExitosa: () -> Unit,
    onIrAHistorial: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val totalVenta = PosCartManager.totalMonto
    val totalArticulos = PosCartManager.totalArticulos
    val items = PosCartManager.items

    var selectedMetodoPago by remember { mutableStateOf(1) }

    val billeteSugeridoMin = ceil(totalVenta / 10.0) * 10.0
    val billete1 = if (billeteSugeridoMin <= totalVenta) billeteSugeridoMin + 10.0 else billeteSugeridoMin
    val billete2 = billete1 + 10.0
    val billete3 = 50.0

    var montoRecibidoText by remember {
        mutableStateOf(if (totalVenta > 0) String.format(Locale.US, "%.2f", billete1) else "0.00")
    }
    val montoRecibido = montoRecibidoText.toDoubleOrNull() ?: 0.0

    val cambio = if (selectedMetodoPago == 1) {
        if (montoRecibido >= totalVenta) montoRecibido - totalVenta else 0.0
    } else {
        0.0
    }

    var isProcessing by remember { mutableStateOf(false) }
    var ventaRegistradaResult by remember { mutableStateOf<VentaResponse?>(null) }
    var showSuccessModal by remember { mutableStateOf(false) }

    fun emitirVenta() {
        if (selectedMetodoPago == 1 && montoRecibido < totalVenta) {
            Toast.makeText(context, "El monto recibido es menor al total de la venta.", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            isProcessing = true
            try {
                val detalles = items.map {
                    DetalleVentaRequest(
                        idProducto = it.producto.idProducto.toLong(),
                        cantidad = it.cantidad,
                        precioUnitarioHistorico = it.producto.displayPrecio
                    )
                }

                val request = VentaRequest(
                    idTurno = UserSession.idTurnoActual,
                    idUsuario = UserSession.idUsuarioGuid,
                    idMetodoPago = selectedMetodoPago.toLong(),
                    montoRecibido = if (selectedMetodoPago == 1) montoRecibido else totalVenta,
                    cambioEntregado = cambio,
                    detalles = detalles
                )

                val response = RetrofitClient.apiService.registrarVenta(
                    authHeader = UserSession.authHeader(),
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    ventaRegistradaResult = response.body()
                    showSuccessModal = true
                } else {
                    val errorMsg = "Error al procesar la venta (${response.code()}): ${response.errorBody()?.string() ?: "Intente nuevamente"}"
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                val errorMsg = "Error de red: ${e.localizedMessage ?: "No se pudo conectar con el servidor"}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            } finally {
                isProcessing = false
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onCancelar) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cancelar",
                                tint = TiendaGoColors.DarkSlate
                            )
                        }
                        Text(
                            text = "Cancelar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.DarkSlate
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ORD-#${PosCartManager.numeroOrden}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = TiendaGoColors.DarkSlate,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "[CAJA #1]",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { emitirVenta() },
                        enabled = !isProcessing && (selectedMetodoPago == 2 || montoRecibido >= totalVenta),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TiendaGoColors.DarkSlate,
                            disabledContainerColor = TiendaGoColors.DarkSlate.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Emitir Ticket y Finalizar Venta",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = TiendaGoColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "[PAGO_FLUJO_03/03]",
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
                        text = "Confirmar Pago",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TiendaGoColors.TextPrimary
                    )

                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TiendaGoColors.PrimaryBlue)
                            )
                            Text(
                                text = "COBRO ACTIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.PrimaryBlue
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = TiendaGoColors.DarkSlate,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Resumen de Venta",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextPrimary
                            )
                        }
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "$totalArticulos ARTÃCULOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.cantidad}x",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.PrimaryBlue,
                                    modifier = Modifier.width(28.dp)
                                )
                                Text(
                                    text = item.producto.displayNombre,
                                    fontSize = 12.sp,
                                    color = TiendaGoColors.TextPrimary
                                )
                            }
                            Text(
                                text = formatPrice(item.producto.displayPrecio * item.cantidad),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextPrimary
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = TiendaGoColors.BorderLight
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Subtotal Neto", fontSize = 12.sp, color = TiendaGoColors.TextSecondary)
                        Text(text = formatPrice(totalVenta), fontSize = 12.sp, color = TiendaGoColors.TextSecondary)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "IVA / Impuestos (Exento 0%)", fontSize = 12.sp, color = TiendaGoColors.TextMuted)
                        Text(text = "$0.00", fontSize = 12.sp, color = TiendaGoColors.TextMuted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL REGISTRADO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Text(
                                text = "Monto a liquidar",
                                fontSize = 11.sp,
                                color = TiendaGoColors.TextMuted
                            )
                        }
                        Text(
                            text = formatPrice(totalVenta),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TiendaGoColors.DarkSlate
                        )
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[MÃ‰TODO_DE_PAGO]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextMuted
                    )
                    Text(
                        text = "Selecciona uno",
                        fontSize = 11.sp,
                        color = TiendaGoColors.TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedMetodoPago = 1 },
                        color = if (selectedMetodoPago == 1) TiendaGoColors.DarkSlate else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (selectedMetodoPago == 1) TiendaGoColors.DarkSlate else TiendaGoColors.BorderLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Efectivo",
                                tint = if (selectedMetodoPago == 1) Color.White else TiendaGoColors.DarkSlate,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Efectivo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMetodoPago == 1) Color.White else TiendaGoColors.DarkSlate
                            )
                            Text(
                                text = if (selectedMetodoPago == 1) "[SELECCIONADO]" else "[DISPONIBLE]",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedMetodoPago == 1) Color(0xFF94A3B8) else TiendaGoColors.TextMuted
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedMetodoPago = 2 },
                        color = if (selectedMetodoPago == 2) TiendaGoColors.DarkSlate else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (selectedMetodoPago == 2) TiendaGoColors.DarkSlate else TiendaGoColors.BorderLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Transferencia",
                                tint = if (selectedMetodoPago == 2) Color.White else TiendaGoColors.DarkSlate,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "QR / Transf.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMetodoPago == 2) Color.White else TiendaGoColors.DarkSlate
                            )
                            Text(
                                text = if (selectedMetodoPago == 2) "[SELECCIONADO]" else "[DISPONIBLE]",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedMetodoPago == 2) Color(0xFF94A3B8) else TiendaGoColors.TextMuted
                            )
                        }
                    }
                }
            }

            if (selectedMetodoPago == 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Monto Recibido del Cliente",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Text(
                                text = "USD / EFECTIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = montoRecibidoText,
                            onValueChange = { montoRecibidoText = it },
                            leadingIcon = {
                                Text(
                                    text = "$",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.DarkSlate,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            trailingIcon = {
                                if (montoRecibidoText.isNotEmpty()) {
                                    IconButton(onClick = { montoRecibidoText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Borrar",
                                            tint = TiendaGoColors.TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = TiendaGoColors.PrimaryBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BILLETES / SUGERIDOS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextSecondary
                            )
                            Text(
                                text = "Toque rÃ¡pido",
                                fontSize = 10.sp,
                                color = TiendaGoColors.TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SuggestionBillChip(
                                label = formatPrice(totalVenta),
                                sublabel = "EXACTO",
                                onClick = { montoRecibidoText = String.format(Locale.US, "%.2f", totalVenta) },
                                modifier = Modifier.weight(1f)
                            )
                            SuggestionBillChip(
                                label = formatPrice(billete1),
                                sublabel = "BILLETE",
                                onClick = { montoRecibidoText = String.format(Locale.US, "%.2f", billete1) },
                                modifier = Modifier.weight(1f)
                            )
                            SuggestionBillChip(
                                label = formatPrice(billete2),
                                sublabel = "BILLETE",
                                onClick = { montoRecibidoText = String.format(Locale.US, "%.2f", billete2) },
                                modifier = Modifier.weight(1f)
                            )
                            SuggestionBillChip(
                                label = formatPrice(billete3),
                                sublabel = "BILLETE",
                                onClick = { montoRecibidoText = String.format(Locale.US, "%.2f", billete3) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Surface(
                    color = if (montoRecibido >= totalVenta) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (montoRecibido >= totalVenta) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (montoRecibido >= totalVenta) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (montoRecibido >= totalVenta) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (montoRecibido >= totalVenta) TiendaGoColors.SuccessGreen else TiendaGoColors.CriticalRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (montoRecibido >= totalVenta) "Cambio a Entregar" else "Monto Insuficiente",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (montoRecibido >= totalVenta) TiendaGoColors.SuccessGreen else TiendaGoColors.CriticalRed
                                )
                                Text(
                                    text = if (montoRecibido >= totalVenta) "Efectivo a devolver al cliente" else "Faltan ${formatPrice(totalVenta - montoRecibido)}",
                                    fontSize = 11.sp,
                                    color = TiendaGoColors.TextSecondary
                                )
                            }
                        }

                        Text(
                            text = formatPrice(cambio),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (montoRecibido >= totalVenta) TiendaGoColors.SuccessGreen else TiendaGoColors.CriticalRed
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CÃ³digo QR Interbancario",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextPrimary
                        )
                        Text(
                            text = "Escanea con cualquier App bancaria o billetera",
                            fontSize = 11.sp,
                            color = TiendaGoColors.TextMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Pago",
                                tint = TiendaGoColors.DarkSlate,
                                modifier = Modifier.size(140.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Monto exacto: ${formatPrice(totalVenta)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessModal) {
        val result = ventaRegistradaResult
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TiendaGoColors.SuccessGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Â¡Venta Completada!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TiendaGoColors.DarkSlate
                    )
                    Text(
                        text = result?.numeroTicket ?: "Ticket Registrado",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.PrimaryBlue
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Cobrado:", fontSize = 13.sp, color = TiendaGoColors.TextSecondary)
                        Text(text = formatPrice(totalVenta), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "MÃ©todo de Pago:", fontSize = 13.sp, color = TiendaGoColors.TextSecondary)
                        Text(text = if (selectedMetodoPago == 1) "Efectivo" else "QR Transferencia", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (selectedMetodoPago == 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Monto Recibido:", fontSize = 13.sp, color = TiendaGoColors.TextSecondary)
                            Text(text = formatPrice(montoRecibido), fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Cambio Entregado:", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TiendaGoColors.SuccessGreen)
                            Text(text = formatPrice(cambio), fontSize = 16.sp, fontWeight = FontWeight.Black, color = TiendaGoColors.SuccessGreen)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Inventario y existencias descontadas automÃ¡ticamente.",
                        fontSize = 11.sp,
                        color = TiendaGoColors.TextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        PosCartManager.vaciar()
                        showSuccessModal = false
                        onVentaExitosa()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TiendaGoColors.DarkSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nueva Venta (POS)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        PosCartManager.vaciar()
                        showSuccessModal = false
                        onIrAHistorial()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver en Historial", fontWeight = FontWeight.Bold, color = TiendaGoColors.DarkSlate)
                }
            }
        )
    }
}

@Composable
fun SuggestionBillChip(
    label: String,
    sublabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TiendaGoColors.TextPrimary
            )
            Text(
                text = sublabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = TiendaGoColors.TextMuted
            )
        }
    }
}
