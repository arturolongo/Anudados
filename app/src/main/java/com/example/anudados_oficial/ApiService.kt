package com.example.anudados_oficial

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

// Clases de respuesta de la API
data class ApiStatusResponse(
    val status: String = "API funcionando correctamente",
    val endpoints: Map<String, String> = mapOf("/predecir" to "POST - Enviar imagen para clasificación"),
    val clases_soportadas: List<String> = listOf(
        "Nudo Ahorcado", "Nudo Calabrote", "Nudo Cote", "Nudo Empaquetador",
        "Nudo Llano", "Nudo Llano Doble", "Nudo Margarita", "Nudo Mariposa",
        "Nudo Pescador", "Nudo Pescador Doble", "Nudo Zarpa de Gato",
        "Nudo de Doble Lazo", "Nudo de Ocho"
    )
)

data class PrediccionResponse(
    val clase: String,
    val probabilidad: Float,
    val tiempo_proceso: Float
)

// Interfaz para llamadas a la API
interface NudosApiService {
    @Multipart
    @POST("predecir")
    suspend fun verificarApi(
        @Part imagen: MultipartBody.Part
    ): Response<PrediccionResponse>
    
    @Multipart
    @POST("predecir")
    suspend fun predecirNudo(
        @Part imagen: MultipartBody.Part
    ): Response<PrediccionResponse>
}

// Cliente de la API
object ApiClient {
    private const val BASE_URL = "http://100.28.103.38:5000/"  // Asegurarse que termina con /
    private const val TIMEOUT_SECONDS = 30L

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: NudosApiService = retrofit.create(NudosApiService::class.java)
}

// Repositorio para manejar las llamadas a la API
class NudosApiRepository {
    private val apiService = ApiClient.service

    suspend fun verificarApi(): Result<PrediccionResponse> {
        return try {
            // Crear una imagen de prueba real (1x1 píxel negro)
            val tempFile = File.createTempFile("test", ".jpg")
            val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            bitmap.setPixel(0, 0, Color.BLACK)
            
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("imagen", tempFile.name, requestFile)

            val response = apiService.verificarApi(body)
            
            // Limpiar recursos
            bitmap.recycle()
            tempFile.delete()
            
            if (response.isSuccessful) {
                response.body()?.let { prediccion ->
                    Result.success(prediccion)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                when (response.code()) {
                    400 -> Result.failure(Exception("Error en la solicitud: formato incorrecto o sin imagen"))
                    500 -> Result.failure(Exception("Error del servidor: ${response.errorBody()?.string()}"))
                    else -> Result.failure(Exception("Error desconocido: ${response.code()}"))
                }
            }
        } catch (e: ConnectException) {
            Result.failure(Exception("No se pudo conectar al servidor. Verifica que:\n1. La API esté en funcionamiento\n2. Estés conectado a la misma red\n3. La dirección IP sea correcta"))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Tiempo de espera agotado. Verifica tu conexión a internet y que la API esté respondiendo"))
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}\nVerifica tu conexión a internet"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }
    
    suspend fun predecirNudo(imagenFile: File): Result<PrediccionResponse> {
        return try {
            // Verificar tamaño de archivo (10MB máximo)
            if (imagenFile.length() > 10 * 1024 * 1024) {
                return Result.failure(Exception("La imagen es demasiado grande. Tamaño máximo: 10MB"))
            }

            // Verificar formato de archivo
            val extension = imagenFile.extension.lowercase()
            if (extension !in listOf("jpg", "jpeg", "png", "webp")) {
                return Result.failure(Exception("Formato de imagen no soportado. Use JPG, JPEG, PNG o WEBP"))
            }

            // Determinar el tipo MIME correcto
            val mimeType = when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/*"
            }

            val requestFile = imagenFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("imagen", imagenFile.name, requestFile)

            val response = apiService.predecirNudo(body)
            
            if (response.isSuccessful) {
                response.body()?.let { prediccion ->
                    Result.success(prediccion)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                when (response.code()) {
                    400 -> Result.failure(Exception("Error en la solicitud: formato incorrecto o sin imagen"))
                    500 -> Result.failure(Exception("Error del servidor: ${response.errorBody()?.string()}"))
                    else -> Result.failure(Exception("Error desconocido: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al procesar la imagen: ${e.message}"))
        }
    }
} 