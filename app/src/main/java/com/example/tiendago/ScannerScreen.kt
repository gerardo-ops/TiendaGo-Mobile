package com.example.tiendago

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.tiendago.data.ProductoResponse
import com.example.tiendago.data.RetrofitClient
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScannerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var productoEncontrado by remember { mutableStateOf<ProductoResponse?>(null) }
    var mensajeEstado by remember { mutableStateOf("Apunta la cámara al código QR o de barras") }
    var yaEscaneado by remember { mutableStateOf(false) }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermisoCamara = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoCamara) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (tienePermisoCamara) {
                CameraPreviewView(
                    onBarcodeScanned = { codigo ->
                        if (!yaEscaneado) {
                            yaEscaneado = true
                            mensajeEstado = "Consultando código: $codigo..."

                            coroutineScope.launch {
                                try {
                                    val respuesta = RetrofitClient.apiService.buscarPorCodigo(codigo)
                                    if (respuesta.isSuccessful && respuesta.body() != null) {
                                        productoEncontrado = respuesta.body()
                                        mensajeEstado = "¡Producto encontrado!"
                                    } else {
                                        mensajeEstado = "No se encontró el producto ($codigo)"
                                    }
                                } catch (e: Exception) {
                                    mensajeEstado = "Error de conexión con la API: ${e.message}"
                                }
                            }
                        }
                    }
                )

                // Recuadro guía
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .align(Alignment.Center)
                        .border(3.dp, Color.Green, RoundedCornerShape(16.dp))
                )

                // Panel inferior
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = mensajeEstado,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        productoEncontrado?.let { prod ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Producto: ${prod.nombre}", style = MaterialTheme.typography.titleMedium)
                            Text(text = "Precio: $${prod.precio} | Stock: ${prod.stock}")
                            Text(text = "Categoría: ${prod.nombreCategoria ?: "General"}")
                        }

                        if (yaEscaneado) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    yaEscaneado = false
                                    productoEncontrado = null
                                    mensajeEstado = "Apunta la cámara al código QR o de barras"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Escanear otro producto")
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Se requiere permiso de la cámara para escanear.")
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewView(onBarcodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val scanner = BarcodeScanning.getClient()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { valor ->
                                        onBarcodeScanned(valor)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}