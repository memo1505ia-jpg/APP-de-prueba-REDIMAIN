package com.example.data.model

/**
 * Categorías militares oficiales de la REDIMAIN.
 * Fuente: Estructura orgánica de la Armada Bolivariana de Venezuela.
 */
object MilitaryCategories {

    val ALL = listOf(
        "OFICIAL DE COMANDO",
        "OFICIAL TÉCNICO",
        "OFICIAL ASIMILADO",
        "OFICIAL DE TROPA",
        "TROPA PROFESIONAL",
        "TROPA ALISTADA"
    )

    val DEFAULT = ALL.first() // "OFICIAL DE COMANDO"

    /** Verifica si una categoría es válida */
    fun isValid(category: String): Boolean = ALL.any {
        it.equals(category.trim(), ignoreCase = true)
    }

    /** Normaliza una categoría legacy al nuevo formato exacto */
    fun normalize(category: String): String {
        val upper = category.trim().uppercase()
        return when {
            upper.contains("COMANDO")              -> "OFICIAL DE COMANDO"
            upper.contains("TÉCNICO") ||
            upper.contains("TECNICO")              -> "OFICIAL TÉCNICO"
            upper.contains("ASIMILADO")            -> "OFICIAL ASIMILADO"
            upper.contains("OFICIAL DE TROPA")     -> "OFICIAL DE TROPA"
            upper.contains("TROPA PROFESIONAL")    -> "TROPA PROFESIONAL"
            upper.contains("TROPA ALISTADA")       -> "TROPA ALISTADA"
            // Legados anteriores
            upper.contains("OFICIALES TÉCNICOS")   -> "OFICIAL TÉCNICO"
            upper.contains("OFICIALES")            -> "OFICIAL DE COMANDO"
            else                                   -> DEFAULT
        }
    }
}
