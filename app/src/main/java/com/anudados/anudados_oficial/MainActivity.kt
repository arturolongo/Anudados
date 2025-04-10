package com.anudados.anudados_oficial

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.anudados_oficial.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS)
        }
        
        try {
            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()
            
            setContent {
                AnudadosTheme {
                    MainScreen()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en onCreate: ${e.message}", e)
            Toast.makeText(this, "Error al iniciar la aplicación", Toast.LENGTH_LONG).show()
        }
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error en onDestroy: ${e.message}", e)
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}

@Composable
fun AnudadosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFF2E7D32),
            primaryVariant = Color(0xFF1B5E20),
            secondary = Color(0xFF4CAF50),
            background = Color.White,
            surface = Color.White
        ),
        content = content
    )
}

@Composable
fun MainScreen() {
    var showScanner by remember { mutableStateOf(false) }
    var showListaNudos by remember { mutableStateOf(false) }
    
    if (showScanner) {
        ScannerScreen(
            onNavigateToLista = { showListaNudos = true },
            onNavigateToHome = { showScanner = false }
        )
    } else if (showListaNudos) {
        SimpleListaNudosScreen(onBack = { showListaNudos = false })
    } else {
        SimpleHomeScreen(
            onScanClick = { showScanner = true },
            onListaClick = { showListaNudos = true }
        )
    }
}

@Composable
fun SimpleHomeScreen(
    onScanClick: () -> Unit,
    onListaClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "ANUDADOS",
                style = MaterialTheme.typography.h3,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = "Escanear Nudo",
                    style = MaterialTheme.typography.h6
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onListaClick,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text(
                    text = "Lista de Nudos",
                    style = MaterialTheme.typography.h6
                )
            }
        }
        
        Text(
            text = "Echo por: Arturito :)",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun ScannerScreen(
    onNavigateToLista: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var apiStatus by remember { mutableStateOf<String?>(null) }
    var isApiConnected by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var predictionResult by remember { mutableStateOf<PrediccionResponse?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Verificar estado de la API al iniciar
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = NudosApiRepository().verificarApi()
                result.onSuccess {
                    isApiConnected = true
                    apiStatus = "API conectada correctamente"
                }.onFailure {
                    isApiConnected = false
                    apiStatus = "Error: ${it.message}"
                }
            } catch (e: Exception) {
                isApiConnected = false
                apiStatus = "Error al verificar API: ${e.message}"
            }
        }
    }
    
    // Crear un archivo temporal para la imagen
    val tempImageFile = remember {
        File(context.getExternalFilesDir(null), "temp_image.jpg")
    }
    
    // Crear el URI para la imagen
    val imageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempImageFile
        )
    }
    
    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Forzar una actualización del bitmap
            capturedImageUri = null
            // Pequeña pausa para asegurar que el archivo se haya guardado
            scope.launch {
                delay(100)
                capturedImageUri = imageUri
            }
            errorMessage = null
        } else {
            errorMessage = "Error al capturar la imagen"
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        try {
            uri?.let {
                capturedImageUri = it
                errorMessage = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar imagen: ${e.message}", e)
            errorMessage = "Error al cargar la imagen"
        }
    }

    // Función para procesar la imagen
    fun processImage(uri: Uri) {
        scope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                val file = if (uri == imageUri) {
                    tempImageFile
                } else {
                    // Si la imagen viene de la galería, copiarla al archivo temporal
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tempImageFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempImageFile
                }
                
                if (file.exists()) {
                    val result = NudosApiRepository().predecirNudo(file)
                    
                    withContext(Dispatchers.Main) {
                        result.onSuccess { response ->
                            predictionResult = response
                            errorMessage = null
                        }.onFailure {
                            errorMessage = it.message
                            predictionResult = null
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "No se pudo acceder al archivo de imagen"
                        predictionResult = null
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error al procesar la imagen: ${e.message}"
                    predictionResult = null
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Escanear Nudo",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Estado de la API
            apiStatus?.let { status ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isApiConnected) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (isApiConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.body2,
                            color = if (isApiConnected) Color(0xFF2E7D32) else Color(0xFFB71C1C)
                        )
                    }
                }
                
                if (!isApiConnected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Se requiere conexión con la API para clasificar nudos. Por favor, verifica tu conexión.",
                        style = MaterialTheme.typography.caption,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mensaje de error
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Vista previa de la imagen
            capturedImageUri?.let { uri ->
                val bitmap = remember(uri) {
                    try {
                        if (uri == imageUri) {
                            // Forzar una nueva lectura del archivo
                            BitmapFactory.decodeFile(tempImageFile.absolutePath)
                        } else {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                BitmapFactory.decodeStream(input)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al cargar imagen: ${e.message}", e)
                        null
                    }
                }
                
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen capturada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No se pudo cargar la imagen")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } ?: run {
                // Si no hay imagen, mostramos un placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Toma una foto o selecciona una imagen para analizar",
                        style = MaterialTheme.typography.body1
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Botones de captura
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        try {
                            // Asegurarse de que el directorio existe
                            if (!tempImageFile.parentFile.exists()) {
                                tempImageFile.parentFile.mkdirs()
                            }
                            cameraLauncher.launch(imageUri)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al iniciar cámara: ${e.message}", e)
                            errorMessage = "Error al abrir la cámara: ${e.message}"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Abrir cámara",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomar foto")
                }
                
                Button(
                    onClick = {
                        try {
                            galleryLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al abrir galería: ${e.message}", e)
                            errorMessage = "Error al abrir la galería"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Galería")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de procesamiento
            Button(
                onClick = {
                    if (capturedImageUri != null && isApiConnected) {
                        processImage(capturedImageUri!!)
                    } else if (!isApiConnected) {
                        errorMessage = "No se puede analizar sin conexión a la API. Por favor, verifica tu conexión a internet y prueba de nuevo."
                    }
                },
                enabled = capturedImageUri != null && !isLoading && isApiConnected,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White,
                    disabledBackgroundColor = Color.Gray
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Analizar Nudo")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Área de resultados
            predictionResult?.let { pred ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFE8F5E9),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nudo detectado:",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = pred.clase,
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confianza: ${(pred.probabilidad * 100).toInt()}%",
                            style = MaterialTheme.typography.h6
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tiempo de procesamiento: ${pred.tiempo_proceso} segundos",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.primaryVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToHome,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primaryVariant,
                    contentColor = Color.White
                )
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun SimpleListaNudosScreen(onBack: () -> Unit) {
    var selectedNudo by remember { mutableStateOf<NudoInfo?>(null) }
    
    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (selectedNudo == null) {
                // Lista de nudos
                Text(
                    text = "Lista de Nudos",
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mostrar la lista de nudos
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    NudosRepository.nudos.forEach { nudo ->
                        NudoCard(nudo = nudo) {
                            selectedNudo = nudo
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primaryVariant,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Volver")
                }
            } else {
                // Detalles del nudo seleccionado
                NudoDetailScreen(nudo = selectedNudo!!) {
                    selectedNudo = null
                }
            }
        }
    }
}

@Composable
fun NudoCard(nudo: NudoInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = Color(0xFFE8F5E9),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del nudo
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colors.primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nudo.nombre.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del nudo
            Column {
                Text(
                    text = nudo.nombre,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Dificultad: ${nudo.dificultad}",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.primaryVariant
                )
            }
        }
    }
}

@Composable
fun NudoDetailScreen(nudo: NudoInfo, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera con título y botón de regreso
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("<", style = MaterialTheme.typography.h5)
            }
            
            Text(
                text = nudo.nombre,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Imagen representativa del nudo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nudo.nombre,
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sección de descripción
        Text(
            text = "Descripción",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = nudo.descripcion,
            style = MaterialTheme.typography.body1
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sección de usos
        Text(
            text = "Usos",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        nudo.usos.forEach { uso ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                )
                
                Text(
                    text = uso,
                    style = MaterialTheme.typography.body1
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nivel de dificultad
        Text(
            text = "Nivel de dificultad",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val nivelColor = when(nudo.dificultad) {
                "Fácil" -> Color(0xFF4CAF50)
                "Intermedia" -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }
            
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(nivelColor, shape = CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = nudo.dificultad,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold,
                color = nivelColor
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botón de volver
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Volver a la lista")
        }
    }
}