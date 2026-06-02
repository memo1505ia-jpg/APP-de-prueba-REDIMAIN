package com.example.firebase

import android.content.Context
import android.net.Uri
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Repositorio para subir archivos a Cloudinary via su API REST sin plan de pago.
 * Usa "unsigned uploads" con un preset público — no requiere clave secreta en el cliente.
 *
 * Variables requeridas en .env:
 *   CLOUDINARY_CLOUD_NAME   → El nombre de tu cloud (ej: "redimain-vznyg")
 *   CLOUDINARY_UPLOAD_PRESET → Nombre del preset de subida sin firma (ej: "redimain_unsigned")
 */
object CloudinaryRepository {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val cloudName: String
        get() = try { BuildConfig.CLOUDINARY_CLOUD_NAME } catch (e: Exception) { "" }

    private val uploadPreset: String
        get() = try { BuildConfig.CLOUDINARY_UPLOAD_PRESET } catch (e: Exception) { "" }

    /** Tipos de archivo permitidos */
    private val allowedMimeTypes = setOf(
        "image/jpeg", "image/jpg", "image/png", "application/pdf"
    )

    /**
     * Sube un archivo a Cloudinary.
     *
     * @param context  Contexto Android para leer el Uri
     * @param uri      Uri del archivo seleccionado por el usuario
     * @param folder   Carpeta destino dentro de Cloudinary ("officer_photos" o "comm_attachments")
     * @return         URL pública segura (https://) del archivo subido, o lanza excepción
     */
    suspend fun uploadFile(context: Context, uri: Uri, folder: String): String {
        return withContext(Dispatchers.IO) {
            if (cloudName.isBlank() || uploadPreset.isBlank()) {
                throw IllegalStateException(
                    "Cloudinary no está configurado. Agrega CLOUDINARY_CLOUD_NAME y " +
                    "CLOUDINARY_UPLOAD_PRESET al archivo .env del proyecto."
                )
            }

            // Verificar tipo de archivo permitido
            val mimeType = context.contentResolver.getType(uri)
                ?: throw IllegalArgumentException("No se pudo determinar el tipo del archivo.")
            if (mimeType !in allowedMimeTypes) {
                throw IllegalArgumentException(
                    "Formato no permitido: $mimeType. Solo se aceptan JPG, PNG y PDF."
                )
            }

            // Leer bytes del archivo
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("No se pudo leer el archivo seleccionado.")

            // Determinar el resource_type según MIME
            val resourceType = if (mimeType == "application/pdf") "raw" else "image"

            val url = "https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    "upload",
                    bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("folder", folder)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
                ?: throw Exception("Respuesta vacía de Cloudinary.")

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(body).optString("error", "Error desconocido")
                } catch (e: Exception) { body }
                throw Exception("Error de Cloudinary (${response.code}): $errorMsg")
            }

            // Extraer la URL segura del JSON de respuesta
            JSONObject(body).getString("secure_url")
        }
    }

    /**
     * Sube foto de tripulante. Solo JPG y PNG.
     */
    suspend fun uploadOfficerPhoto(context: Context, uri: Uri): String =
        uploadFile(context, uri, "officer_photos")

    /**
     * Sube adjunto de comunicación (oficio). JPG, PNG o PDF.
     */
    suspend fun uploadCommAttachment(context: Context, uri: Uri): String =
        uploadFile(context, uri, "comm_attachments")
}
