package com.example.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * Singleton que centraliza el acceso a Firebase Auth y Firestore.
 * El almacenamiento de archivos se maneja via Cloudinary (ver CloudinaryRepository.kt).
 */
object FirebaseManager {

    /** Servicio de autenticación Firebase */
    val auth: FirebaseAuth by lazy { Firebase.auth }

    /** Base de datos en la nube Firestore (usuarios, permisos, datos sincronizados) */
    val db: FirebaseFirestore by lazy { Firebase.firestore }

    /** Colecciones de Firestore */
    object Collections {
        const val USERS        = "users"
        const val OFFICERS     = "officers"
        const val COMM_TRAFFIC = "comm_traffic"
    }
}
