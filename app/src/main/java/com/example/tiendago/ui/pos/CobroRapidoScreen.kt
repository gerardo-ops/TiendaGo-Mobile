package com.example.tiendago.ui.pos

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.tiendago.data.ProductoResponse
import com.example.tiendago.data.RetrofitClient
import com.example.tiendago.ui.components.CategoryFilterRow
import com.example.tiendago.ui.components.TiendaGoBottomBar
import com.example.tiendago.ui.components.TiendaGoColors
import com.example.tiendago.ui.components.TiendaGoTopBar
import com.example.tiendago.ui.components.formatPrice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobroRapidoScreen(
    onNavigateToConfirmarPago: () -> Unit,
    onNavigateToScanner: () -> Unit = {},
    onTabSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showTicketModal by remember { mutableStateOf(false) }
    var showVaciarDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var productosList by remember { mutableStateOf<List<ProductoResponse>>(emptyList()) }

    val mockPosProductos = remember {
        listOf(
            ProductoResponse(
                idProducto = 1,
                codigoBarra = "44021",
                nombre = "Coca-Cola 500ml",
                precio = 1.50,
                stock = 42,
                idCategoria = 1,
                nombreCategoria = "Bebidas"
            ),
            ProductoResponse(
                idProducto = 2,
                codigoBarra = "99014",
                nombre = "Papas ClÃ¡sicas 115g",
                precio = 2.00,
                stock = 8,
                idCategoria = 2,
                nombreCategoria = "Snacks"
            ),
            ProductoResponse(
                idProducto = 3,
                codigoBarra = "31209",
                nombre = "Arroz Extra 1kg",
                precio = 1.80,
                stock = 65,
                idCategoria = 3,
                nombreCategoria = "Abarrotes"
            ),
            ProductoResponse(
                idProducto = 4,
                codigoBarra = "10822",
                nombre = "Leche Entera 1L",
                precio = 1.40,
                stock = 12,
                idCategoria = 4,
                nombreCategoria = "LÃ¡cteos"
            ),
            ProductoResponse(
                idProducto = 5,
                codigoBarra = "77215",
                nombre = "Pan Blanco Molde",
                precio = 2.20,
                stock = 15,
                idCategoria = 6,
                nombreCategoria = "PanaderÃ­a"
            ),
            ProductoResponse(
                idProducto = 6,
                codigoBarra = "88143",
                nombre = "Agua Mineral 600ml",
                precio = 0.80,
                stock = 50,
                idCategoria = 1,
                nombreCategoria = "Bebidas"
            ),
            ProductoResponse(
                idProducto = 7,
                codigoBarra = "66501",
                nombre = "Chocolate Barra 80g",
                precio = 1.75,
                stock = 20,
                idCategoria = 2,
                nombreCategoria = "Snacks"
            ),
            ProductoResponse(
                idProducto = 8,
                codigoBarra = "55412",
                nombre = "JabÃ³n LÃ­quido 250ml",
                precio = 2.10,
                stock = 18,
                idCategoria = 5,
                nombreCategoria = "Limpieza"
            )
        )
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = RetrofitClient.apiService.getProductosActivos()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                productosList = response.body()!!
            } else {
                productosList = mockPosProductos
            }
        } catch (e: Exception) {
            productosList = mockPosProductos
        } finally {
            isLoading = false
        }
    }

    val categorias = listOf("Todos", "Bebidas", "Snacks", "Abarrotes", "LÃ¡cteos", "PanaderÃ­a")
    val filteredProductos = remember(productosList, selectedCategory, searchQuery) {
        var list = if (selectedCategory == "Todos") {
            productosList
        } else {
            productosList.filter { it.displayCategoria.equals(selectedCategory, ignoreCase = true) }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.displayNombre.contains(searchQuery, ignoreCase = true) ||
                it.displayCodigo.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    Scaffold(
        topBar = {
            Column {
                TiendaGoTopBar(
                    screenTitle = "Caja / Terminal POS",
                    tagSubtitle = "[TIENDAGO]",
                    onSearchClick = {},
                    onProfileClick = {}
                )

                Surface(
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "[POS_TERM_01]",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.DarkSlate,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TiendaGoColors.SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EN LÃNEA â€¢ CAJA #1",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.DarkSlate
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (PosCartManager.totalArticulos > 0) {
                                            showVaciarDialog = true
                                        } else {
                                            Toast.makeText(context, "El carrito ya estÃ¡ vacÃ­o", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Vaciar",
                                    tint = TiendaGoColors.CriticalRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Vaciar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.CriticalRed
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onNavigateToScanner() },
                                color = TiendaGoColors.DarkSlate
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Escanear",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                Surface(
                    color = TiendaGoColors.DarkSlate,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${PosCartManager.totalArticulos} ARTÃCULOS",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Subtotal sin impuestos",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TOTAL VENTA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = formatPrice(PosCartManager.totalMonto),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showTicketModal = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Ver ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (PosCartManager.totalArticulos > 0) {
                                        onNavigateToConfirmarPago()
                                    } else {
                                        Toast.makeText(context, "AÃ±ade al menos un producto para cobrar", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = PosCartManager.totalArticulos > 0,
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = TiendaGoColors.DarkSlate,
                                    disabledContainerColor = Color.White.copy(alpha = 0.3f),
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cobrar ${formatPrice(PosCartManager.totalMonto)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                TiendaGoBottomBar(
                    currentTab = "Caja",
                    onTabSelected = onTabSelected
                )
            }
        },
        containerColor = TiendaGoColors.Background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cobro RÃ¡pido",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TiendaGoColors.TextPrimary
                        )

                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ORD-#${PosCartManager.numeroOrden}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "Selecciona Ã­tems para registrar venta directa",
                        fontSize = 12.sp,
                        color = TiendaGoColors.TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por nombre, cÃ³digo o SKU...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TiendaGoColors.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onNavigateToScanner) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Escanear",
                                    tint = TiendaGoColors.DarkSlate,
                                    modifier = Modifier.size(20.dp)
                                )
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                CategoryFilterRow(
                    categories = categorias,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATÃLOGO RÃPIDO (${filteredProductos.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextSecondary
                    )
                    Text(
                        text = "ðŸ‘† Pulsa + para aÃ±adir",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TiendaGoColors.PrimaryBlue
                    )
                }
            }

            items(filteredProductos, key = { it.idProducto }) { producto ->
                val cantidadEnOrden = PosCartManager.getCantidad(producto.idProducto)

                PosProductCard(
                    producto = producto,
                    cantidad = cantidadEnOrden,
                    onIncrementar = { PosCartManager.agregar(producto) },
                    onDecrementar = { PosCartManager.disminuir(producto) }
                )
            }
        }
    }

    if (showVaciarDialog) {
        AlertDialog(
            onDismissRequest = { showVaciarDialog = false },
            title = { Text("Â¿Vaciar orden actual?") },
            text = { Text("Se removerÃ¡n todos los artÃ­culos de la orden #ORD-${PosCartManager.numeroOrden}.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        PosCartManager.vaciar()
                        showVaciarDialog = false
                        Toast.makeText(context, "Orden vaciada", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Vaciar", color = TiendaGoColors.CriticalRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaciarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showTicketModal) {
        ModalBottomSheet(
            onDismissRequest = { showTicketModal = false },
            containerColor = Color.White
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
                        text = "Resumen de la Orden",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextPrimary
                    )
                    Text(
                        text = "#ORD-${PosCartManager.numeroOrden}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.PrimaryBlue
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (PosCartManager.items.isEmpty()) {
                    Text(
                        text = "No hay productos en la orden.",
                        fontSize = 13.sp,
                        color = TiendaGoColors.TextMuted,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    PosCartManager.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.cantidad}x ${item.producto.displayNombre}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.TextPrimary
                                )
                                Text(
                                    text = "Unitario: ${formatPrice(item.producto.displayPrecio)}",
                                    fontSize = 11.sp,
                                    color = TiendaGoColors.TextMuted
                                )
                            }
                            Text(
                                text = formatPrice(item.producto.displayPrecio * item.cantidad),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextPrimary
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total a Cobrar:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TiendaGoColors.TextPrimary
                        )
                        Text(
                            text = formatPrice(PosCartManager.totalMonto),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TiendaGoColors.PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        showTicketModal = false
                        if (PosCartManager.totalArticulos > 0) {
                            onNavigateToConfirmarPago()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TiendaGoColors.DarkSlate)
                ) {
                    Text("Proceder al Cobro", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PosProductCard(
    producto: ProductoResponse,
    cantidad: Int,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onIncrementar() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.dp,
            if (cantidad > 0) TiendaGoColors.DarkSlate else TiendaGoColors.BorderLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = TiendaGoColors.DarkSlate,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (cantidad > 0) {
                    Surface(
                        color = TiendaGoColors.DarkSlate,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "$cantidad en orden",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = producto.displayNombre,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TiendaGoColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(34.dp)
            )

            Text(
                text = "SKU-${producto.displayCodigo.take(6)}",
                fontSize = 10.sp,
                color = TiendaGoColors.TextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatPrice(producto.displayPrecio),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TiendaGoColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = cantidad > 0) { onDecrementar() },
                    color = if (cantidad > 0) Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Menos",
                            tint = if (cantidad > 0) TiendaGoColors.DarkSlate else TiendaGoColors.TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "$cantidad",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cantidad > 0) TiendaGoColors.DarkSlate else TiendaGoColors.TextMuted
                )

                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onIncrementar() },
                    color = TiendaGoColors.DarkSlate,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "MÃ¡s",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
