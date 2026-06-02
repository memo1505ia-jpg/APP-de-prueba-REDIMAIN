package com.example.data.repository

import com.example.data.model.*

object SampleData {
    val defaultStatusOptions = listOf(
        StatusOptionEntity(name = "Activo"),
        StatusOptionEntity(name = "Permiso especial"),
        StatusOptionEntity(name = "Reposo"),
        StatusOptionEntity(name = "Reposo médico domiciliario")
    )

    val defaultUser = UserEntity(
        email = "memo1505matuteramayo@gmail.com",
        name = "Almirante de Guardia",
        role = UserRole.SUPER_ADMIN.name
    )

    val defaultFuelLimits = listOf(
        FuelLimitEntity(plateOrPerson = "AMB-24A", weeklyLimitLiters = 80.0),
        FuelLimitEntity(plateOrPerson = "REDIMAIN-01", weeklyLimitLiters = 150.0),
        FuelLimitEntity(plateOrPerson = "V/A Rogelio Montenegro", weeklyLimitLiters = 80.0)
    )

    val defaultFuelTransactions = listOf(
        FuelTransactionEntity(
            id = 1,
            personName = "V/A Rogelio Montenegro",
            vehicleName = "Toyota Hilux Armada",
            plate = "AMB-24A",
            liters = 45.0,
            fuelType = "Gasolina",
            dateString = "2026-05-25"
        ),
        FuelTransactionEntity(
            id = 2,
            personName = "C/A Virginia Rivas",
            vehicleName = "Jeep Wrangler Guardacostas",
            plate = "REDIMAIN-01",
            liters = 60.0,
            fuelType = "Diésel",
            dateString = "2026-05-26"
        ),
        FuelTransactionEntity(
            id = 3,
            personName = "V/A Rogelio Montenegro",
            vehicleName = "Chevrolet Tahoe",
            plate = "REDIMAIN-02",
            liters = 40.0,
            fuelType = "Gasolina",
            dateString = "2026-05-27"
        )
    )

    val defaultTabs = listOf(
        TabEntity(
            id = 1,
            name = "Misiones de Patrullaje",
            columns = listOf("Fecha", "Unidad Naviera (Buque)", "Coordenadas o Sector", "Alerta", "Estatus")
        ),
        TabEntity(
            id = 2,
            name = "Personal y Guardia Navales",
            columns = listOf("Comandancia Rango", "Nombre y Apellidos", "Componente / Rol", "Disponibilidad", "Última Novedad")
        ),
        TabEntity(
            id = 3,
            name = "Control Logístico y Suministros",
            columns = listOf("Fecha Límite", "Suministro / Unidad", "Cantidades", "Departamento Responsable", "Estatus de Abasto")
        )
    )

    val defaultRows = mapOf(
        1 to listOf(
            RowEntity(
                tabId = 1,
                cells = listOf("2026-05-22", "AB Yecuana (PO-13)", "Sector Oriental / Faja Petrolera", "Media", "En Ejecución"),
                isConfidential = true
            ),
            RowEntity(
                tabId = 1,
                cells = listOf("2026-05-21", "Guardacostas Carúpano (PG-52)", "Fachada Atlántica Venezolana", "Baja", "Completado"),
                isConfidential = false
            )
        ),
        2 to listOf(
            RowEntity(
                tabId = 2,
                cells = listOf("Capitán de Navío", "Rogelio Montenegro", "Armada Bolivariana (ARB)", "Disponible", "Reporte de patrulla costera diurna sin novedad."),
                isConfidential = false
            ),
            RowEntity(
                tabId = 2,
                cells = listOf("Almirante Comandante", "Matute Ramayo Memo", "REDIMAIN Principal", "Activo", "Supervisión directa de las operaciones marítimas del Atlántico."),
                isConfidential = true
            )
        ),
        3 to listOf(
            RowEntity(
                tabId = 3,
                cells = listOf("2026-05-25", "Combustible diésel - Fragata F-21", "45,000 Litros", "Logística Terrestre y Naval", "Planificado"),
                isConfidential = false
            ),
            RowEntity(
                tabId = 3,
                cells = listOf("2026-05-28", "Cascos tácticos y chalecos de rescate", "120 Sets", "Sostenimiento Operativo", "En Ejecución"),
                isConfidential = false
            )
        )
    )

    val defaultOfficers = listOf(
        OfficerEntity(
            name = "V/A Rogelio Montenegro",
            cedula = "V-12.845.392",
            phone = "+58 412 837 2891",
            rankCargo = "Jefe del Estado Mayor REDIMAIN",
            status = "Activo",
            statusFromDate = "2026-05-01",
            statusToDate = "2026-12-31",
            hasAlarm = false
        ),
        OfficerEntity(
            name = "C/A Virginia Rivas",
            cedula = "V-14.772.311",
            phone = "+58 414 742 7831",
            rankCargo = "Comandante de la Estación Principal Guardacostas",
            status = "Permiso especial",
            statusFromDate = "2026-05-22",
            statusToDate = "2026-05-30",
            hasAlarm = true
        ),
        OfficerEntity(
            name = "T/N Francisco Pérez",
            cedula = "V-18.992.833",
            phone = "+58 424 901 2288",
            rankCargo = "Jefe Seccional Inteligencia Electrónica",
            status = "Reposo médico domiciliario",
            statusFromDate = "2026-05-24",
            statusToDate = "2026-05-28",
            hasAlarm = true
        )
    )

    val defaultCommTraffic = listOf(
        CommTrafficEntity(
            documentNumber = "MPPD-OF-REDIMAIN-0824",
            subject = "Plan de Operaciones Fachada Atlántica S-02",
            origin = "Ministerio del Poder Popular de la Defensa (MPPD)",
            recipientRankName = "V/A Rogelio Montenegro",
            dateReceived = "2026-05-23",
            deadlineDate = "2026-05-31",
            hasDeadlineAlarm = true,
            status = "Ejecutando"
        ),
        CommTrafficEntity(
            documentNumber = "COMAN-INST-0312",
            subject = "Inspección de Buques de Carga Extranjeros",
            origin = "Comando Naval de Operaciones",
            recipientRankName = "C/A Virginia Rivas",
            dateReceived = "2026-05-21",
            deadlineDate = "2026-05-28",
            hasDeadlineAlarm = true,
            status = "Pendiente"
        )
    )
}
