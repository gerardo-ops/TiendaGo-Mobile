package com.example.tiendago.ui.inventario

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.tiendago.data.ProductoResponse
import com.example.tiendago.data.RetrofitClient
import com.example.tiendago.data.UserSession
import com.example.tiendago.ui.components.CategoryFilterRow
import com.example.tiendago.ui.components.TiendaGoBottomBar
import com.example.tiendago.ui.components.TiendaGoColors
import com.example.tiendago.ui.components.TiendaGoTopBar
import com.example.tiendago.ui.components.formatPrice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    onNavigateToNuevoProducto: () -> Unit,
    onNavigateToScanner: () -> Unit = {},
    onTabSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var sortAscendingStock by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var productosList by remember { mutableStateOf<List<ProductoResponse>>(emptyList()) }

    fun cargarProductos() {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getProductos()
                if (response.isSuccessful) {
                    productosList = response.body() ?: emptyList()
                } else {
                    productosList = emptyList()
                    Toast.makeText(context, "Error al cargar productos (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                productosList = emptyList()
                Toast.makeText(context, "Error de red: ${e.localizedMessage ?: "No se pudo conectar"}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarProductos()
    }

    val categorias = listOf("Todos", "Bebidas", "Snacks", "Abarrotes", "Lácteos", "Limpieza")
    val countsByCategory = remember(productosList) {
        val map = mutableMapOf<String, Int>()
        map["Todos"] = productosList.size
        categorias.filter { it != "Todos" }.forEach { cat ->
            map[cat] = productosList.count { it.displayCategoria.equals(cat, ignoreCase = true) }
        }
        map
    }

    val filteredProductos = remember(productosList, selectedCategory, searchQuery, sortAscendingStock) {
        var list = if (selectedCategory == "Todos") {
            productosList
        } else {
            productosList.filter { it.displayCategoria.equals(selectedCategory, ignoreCase = true) }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.displayNombre.contains(searchQuery, ignoreCase = true) ||
                it.displayCodigo.contains(searchQuery, ignoreCase = true) ||
                it.displayCategoria.contains(searchQuery, ignoreCase = true)
            }
        }

        if (sortAscendingStock) {
            list.sortedBy { it.displayStock }
        } else {
            list.sortedByDescending { it.displayStock }
        }
    }

    Scaffold(
        topBar = {
            TiendaGoTopBar(
                screenTitle = "Inventario",
                tagSubtitle = "[TIENDAGO]",
                onSearchClick = {},
                onProfileClick = {
                    Toast.makeText(context, "Usuario: ${UserSession.nombre} (${UserSession.rol})", Toast.LENGTH_SHORT).show()
                }
            )
        },
        bottomBar = {
            TiendaGoBottomBar(
                currentTab = "Productos",
                onTabSelected = onTabSelected
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNuevoProducto,
                containerColor = TiendaGoColors.DarkSlate,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Producto",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = TiendaGoColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Principal (Wireframe 1)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Inventario",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TiendaGoColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "v2.4",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${productosList.size} ITEMS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TiendaGoColors.TextSecondary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TiendaGoColors.SuccessGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SYNC ACTIVO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TiendaGoColors.SuccessGreen
                                )
                            }
                        }
                    }

                    Text(
                        text = "Catálogo y existencias en tienda",
                        fontSize = 13.sp,
                        color = TiendaGoColors.TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Buscador y botones de Escáner y Recarga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Buscar por nombre, SKU o cód...",
                                    fontSize = 13.sp,
                                    color = TiendaGoColors.TextMuted
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TiendaGoColors.TextMuted,
                                    modifier = Modifier.size(20.dp)
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
                            modifier = Modifier.weight(1f)
                        )

                        // Botón de Escáner de Barras
                        Surface(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateToScanner() },
                            color = Color.White,
                            border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Escanear Código",
                                    tint = TiendaGoColors.DarkSlate,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Botón de Refresco
                        Surface(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { cargarProductos() },
                            color = Color.White,
                            border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Recargar Catálogo",
                                    tint = if (isLoading) TiendaGoColors.PrimaryBlue else TiendaGoColors.DarkSlate,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Chips de categorías (Wireframe 1)
            item {
                CategoryFilterRow(
                    categories = categorias,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    counts = countsByCategory,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Subheader de listado
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LISTADO GENERAL (${filteredProductos.size} VISIBLES)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { sortAscendingStock = !sortAscendingStock }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (sortAscendingStock) "Stock: Menor a Mayor" else "Stock: Mayor a Menor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TiendaGoColors.PrimaryBlue
                        )
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Ordenar",
                            tint = TiendaGoColors.PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Lista de tarjetas
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = TiendaGoColors.PrimaryBlue,
                            strokeWidth = 3.dp
                        )
                    }
                }
            } else if (filteredProductos.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = TiendaGoColors.TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay productos registrados en inventario",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredProductos, key = { it.idProducto }) { producto ->
                    ProductInventoryCard(
                        producto = producto,
                        onMenuClick = {
                            Toast.makeText(context, "${producto.displayNombre}: SKU ${producto.displayCodigo}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductInventoryCard(
    producto: ProductoResponse,
    onMenuClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TiendaGoColors.BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconVector = when (producto.displayCategoria.lowercase()) {
                "bebidas" -> Icons.Default.LocalDrink
                "snacks" -> Icons.Default.Cookie
                "abarrotes" -> Icons.Default.Inventory2
                "lácteos", "lacteos" -> Icons.Default.WaterDrop
                "limpieza" -> Icons.Default.CleaningServices
                else -> Icons.Default.ShoppingBag
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = TiendaGoColors.DarkSlate,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = producto.displayCodigo.take(6),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${producto.displayCodigo}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TiendaGoColors.TextMuted
                    )

                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = producto.displayCategoria.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = producto.displayNombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TiendaGoColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatPrice(producto.displayPrecio),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TiendaGoColors.TextPrimary
                    )

                    if (producto.costoCompra > 0.0) {
                        Text(
                            text = "Costo: ${formatPrice(producto.costoCompra)}",
                            fontSize = 11.sp,
                            color = TiendaGoColors.TextMuted
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(56.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = TiendaGoColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (producto.esCritico) {
                    Surface(
                        color = TiendaGoColors.DarkSlate,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alerta",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${producto.displayStock} uds • Crítico",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "• Stock: ${producto.displayStock} uds",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
