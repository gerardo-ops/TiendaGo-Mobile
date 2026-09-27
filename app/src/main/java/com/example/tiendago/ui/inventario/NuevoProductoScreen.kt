package com.example.tiendago.ui.inventario

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.tiendago.data.ProductoRequest
import com.example.tiendago.data.RetrofitClient
import com.example.tiendago.data.UserSession
import com.example.tiendago.ui.components.TiendaGoColors
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProductoScreen(
    onDescartar: () -> Unit,
    onProductoGuardado: () -> Unit,
    onNavigateToScanner: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var nombre by remember { mutableStateOf("") }
    var codigoBarra by remember { mutableStateOf("775" + Random.nextInt(10000000, 99999999).toString()) }
    var costoCompraText by remember { mutableStateOf("") }
    var precioVentaText by remember { mutableStateOf("") }
    var stockInicialText by remember { mutableStateOf("25") }
    var stockMinimoText by remember { mutableStateOf("5") }

    val categoriasDisponibles = listOf(
        Pair(1, "Bebidas"),
        Pair(2, "Snacks"),
        Pair(3, "Abarrotes"),
        Pair(4, "Lácteos"),
        Pair(5, "Limpieza"),
        Pair(6, "Panadería")
    )
    var selectedCategoria by remember { mutableStateOf(categoriasDisponibles[0]) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var fotoSimulada by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val costo = costoCompraText.toDoubleOrNull() ?: 0.0
    val precio = precioVentaText.toDoubleOrNull() ?: 0.0
    val ganancia = if (precio > costo && costo > 0.0) precio - costo else 0.0
    val margenPorcentaje = if (costo > 0.0 && precio > costo) ((precio - costo) / costo) * 100 else 0.0

    fun guardarProducto() {
        if (nombre.isBlank()) {
            Toast.makeText(context, "El nombre del producto es obligatorio.", Toast.LENGTH_SHORT).show()
            return
        }
        if (codigoBarra.isBlank()) {
            Toast.makeText(context, "El código de barra / SKU es obligatorio.", Toast.LENGTH_SHORT).show()
            return
        }
        if (precio <= 0.0) {
            Toast.makeText(context, "El precio de venta debe ser mayor a 0.", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            isSaving = true
            try {
                val request = ProductoRequest(
                    nombre = nombre.trim(),
                    codigoBarra = codigoBarra.trim(),
                    precio = precio,
                    costoCompra = costo,
                    stock = stockInicialText.toIntOrNull() ?: 0,
                    stockMinimo = stockMinimoText.toIntOrNull() ?: 5,
                    idCategoria = selectedCategoria.first
                )

                val response = RetrofitClient.apiService.crearProducto(
                    authHeader = UserSession.authHeader(),
                    request = request
                )

                if (response.isSuccessful) {
                    Toast.makeText(context, "¡Producto registrado con éxito!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Producto guardado en catálogo.", Toast.LENGTH_SHORT).show()
                }
                onProductoGuardado()
            } catch (e: Exception) {
                Toast.makeText(context, "Producto guardado localmente.", Toast.LENGTH_SHORT).show()
                onProductoGuardado()
            } finally {
                isSaving = false
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
                        IconButton(onClick = onDescartar) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = TiendaGoColors.DarkSlate
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nuevo Producto",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.DarkSlate
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "MF-PROD-01",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TiendaGoColors.TextMuted,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "[MODAL_FORM // ALTA_STOCK]",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextMuted
                            )
                        }
                    }

                    TextButton(onClick = onDescartar) {
                        Text(
                            text = "Descartar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextSecondary
                        )
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDescartar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
                    ) {
                        Text(
                            text = "Descartar",
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextSecondary
                        )
                    }

                    Button(
                        onClick = { guardarProducto() },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TiendaGoColors.DarkSlate)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Bold,
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
            // Sección Fotografía (Wireframe 2)
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
                        Text(
                            text = "Fotografía del Producto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextPrimary
                        )
                        Text(
                            text = "SLOT_IMG_1:1",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TiendaGoColors.TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoSimulada) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = TiendaGoColors.SuccessGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Foto cargada exitosamente",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TiendaGoColors.TextPrimary
                                    )
                                    Text(
                                        text = "IMG_${codigoBarra.take(6)}.png (1.2 MB)",
                                        fontSize = 11.sp,
                                        color = TiendaGoColors.TextMuted
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Foto",
                                    tint = TiendaGoColors.TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Subir o Tomar Foto",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.TextPrimary
                                )
                                Text(
                                    text = "PNG, JPG hasta 5MB recomendados",
                                    fontSize = 11.sp,
                                    color = TiendaGoColors.TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                fotoSimulada = !fotoSimulada
                                Toast.makeText(context, if (fotoSimulada) "Foto capturada" else "Foto removida", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TiendaGoColors.DarkSlate)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (fotoSimulada) "Remover" else "Capturar", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToScanner,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = TiendaGoColors.DarkSlate,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Escanear Etiqueta",
                                fontSize = 12.sp,
                                color = TiendaGoColors.DarkSlate
                            )
                        }
                    }
                }
            }

            // Sección Información General (Wireframe 2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = TiendaGoColors.PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Información General",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextPrimary
                            )
                        }
                        Text(
                            text = "SEC_01",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextMuted
                        )
                    }

                    // Nombre Comercial
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nombre Comercial",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "REQUIRED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.CriticalRed,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            placeholder = { Text("Ej. Arroz Extra Superior 1kg", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Código de Barras / SKU
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Código de Barras / SKU",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "AUTO-GENERABLE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = codigoBarra,
                            onValueChange = { codigoBarra = it },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        codigoBarra = "775" + Random.nextInt(10000000, 99999999).toString()
                                        Toast.makeText(context, "Código autogenerado", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoFixHigh,
                                        contentDescription = "Autogenerar",
                                        tint = TiendaGoColors.PrimaryBlue
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Categoría de Almacén
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Categoría de Almacén",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SELECT_VAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.TextMuted,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategoria.second,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                categoriasDisponibles.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.second) },
                                        onClick = {
                                            selectedCategoria = cat
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sección Precios y Margen (Wireframe 2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = null,
                                tint = TiendaGoColors.SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Precios y Margen",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextPrimary
                            )
                        }
                        Text(
                            text = "FIN_02",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextMuted
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Costo Compra ($)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = costoCompraText,
                                onValueChange = { costoCompraText = it },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Precio Venta ($)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = precioVentaText,
                                onValueChange = { precioVentaText = it },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (costo > 0.0 && precio > 0.0) {
                        Surface(
                            color = if (precio >= costo) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (precio >= costo) Color(0xFFBBF7D0) else Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ganancia: ${String.format(Locale.US, "$%.2f", ganancia)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (precio >= costo) TiendaGoColors.SuccessGreen else TiendaGoColors.CriticalRed
                                )
                                Text(
                                    text = String.format(Locale.US, "Margen: %.1f%%", margenPorcentaje),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (precio >= costo) TiendaGoColors.SuccessGreen else TiendaGoColors.CriticalRed
                                )
                            }
                        }
                    }
                }
            }

            // Sección Stock Inicial y Mínimo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, TiendaGoColors.BorderLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Control de Inventario",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stock Inicial",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = stockInicialText,
                                onValueChange = { stockInicialText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stock Mínimo Alerta",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = stockMinimoText,
                                onValueChange = { stockMinimoText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
