package com.example.tiendago.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// --- Paleta de colores estándar TiendaGo ---
object TiendaGoColors {
    val Background = Color(0xFFF8FAFC)
    val SurfaceCard = Color.White
    val DarkSlate = Color(0xFF0F172A)
    val SlateHeader = Color(0xFF1E293B)
    val PrimaryBlue = Color(0xFF2563EB)
    val PrimaryAccent = Color(0xFF38BDF8)
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)
    val BorderLight = Color(0xFFE2E8F0)
    val CriticalRed = Color(0xFFDC2626)
    val CriticalRedBg = Color(0xFFFEE2E2)
    val SuccessGreen = Color(0xFF16A34A)
    val SuccessGreenBg = Color(0xFFDCFCE7)
    val ChipUnselected = Color(0xFFF1F5F9)
    val ChipSelected = Color(0xFF0F172A)
}

fun formatPrice(amount: Double): String {
    return String.format(Locale.US, "$%.2f", amount)
}

@Composable
fun TiendaGoTopBar(
    screenTitle: String = "Inicio",
    tagSubtitle: String = "[TIENDAGO]",
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "TiendaGo Logo",
                        tint = TiendaGoColors.DarkSlate,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TiendaGo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TiendaGoColors.DarkSlate
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tagSubtitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TiendaGoColors.TextMuted
                        )
                    }
                    Text(
                        text = screenTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TiendaGoColors.PrimaryBlue
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TiendaGoColors.TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TiendaGoColors.DarkSlate)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TiendaGoBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("Inicio", Icons.Default.Home, "Inicio"),
        Triple("Productos", Icons.Default.Inventory2, "Productos"),
        Triple("Caja", Icons.Default.PointOfSale, "Caja"),
        Triple("Historial", Icons.Default.History, "Historial")
    )

    NavigationBar(
        containerColor = Color.White,
        contentColor = TiendaGoColors.TextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        items.forEach { (tabName, icon, label) ->
            val isSelected = currentTab == tabName
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tabName) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = TiendaGoColors.PrimaryBlue,
                    indicatorColor = TiendaGoColors.DarkSlate,
                    unselectedIconColor = TiendaGoColors.TextMuted,
                    unselectedTextColor = TiendaGoColors.TextMuted
                )
            )
        }
    }
}

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    counts: Map<String, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat == selectedCategory
            val count = counts[cat]
            val countText = if (count != null && count > 0) " $count" else ""

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectCategory(cat) },
                color = if (isSelected) TiendaGoColors.ChipSelected else TiendaGoColors.ChipUnselected,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$cat$countText",
                    color = if (isSelected) Color.White else TiendaGoColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
