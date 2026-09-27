package com.example.tiendago.ui.pos

import androidx.compose.runtime.mutableStateListOf
import com.example.tiendago.data.ProductoResponse

data class PosCartItem(
    val producto: ProductoResponse,
    var cantidad: Int
)

object PosCartManager {
    val items = mutableStateListOf<PosCartItem>()
    var numeroOrden: String = (1000..9999).random().toString()

    fun agregar(producto: ProductoResponse) {
        val index = items.indexOfFirst { it.producto.idProducto == producto.idProducto }
        if (index >= 0) {
            val actual = items[index]
            items[index] = actual.copy(cantidad = actual.cantidad + 1)
        } else {
            items.add(PosCartItem(producto, 1))
        }
    }

    fun disminuir(producto: ProductoResponse) {
        val index = items.indexOfFirst { it.producto.idProducto == producto.idProducto }
        if (index >= 0) {
            val actual = items[index]
            if (actual.cantidad > 1) {
                items[index] = actual.copy(cantidad = actual.cantidad - 1)
            } else {
                items.removeAt(index)
            }
        }
    }

    fun getCantidad(productoId: Int): Int {
        return items.firstOrNull { it.producto.idProducto == productoId }?.cantidad ?: 0
    }

    fun vaciar() {
        items.clear()
        numeroOrden = (1000..9999).random().toString()
    }

    val totalArticulos: Int
        get() = items.sumOf { it.cantidad }

    val totalMonto: Double
        get() = items.sumOf { it.producto.displayPrecio * it.cantidad }
}
