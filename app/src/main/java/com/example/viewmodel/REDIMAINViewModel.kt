package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import com.example.data.database.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.RowEntity
import com.example.data.model.TabEntity
import com.example.data.repository.MilitaryAgendaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.data.model.OfficerEntity
import com.example.data.model.StatusOptionEntity
import com.example.data.model.CommTrafficEntity
import com.example.data.model.UserEntity
import com.example.data.model.FuelTransactionEntity
import com.example.data.model.FuelLimitEntity

class REDIMAINViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MilitaryAgendaRepository

    val tabsState: StateFlow<List<TabEntity>>
    val chatMessagesState: StateFlow<List<ChatMessageEntity>>
    val officersState: StateFlow<List<OfficerEntity>>
    val statusOptionsState: StateFlow<List<StatusOptionEntity>>
    val commTrafficState: StateFlow<List<CommTrafficEntity>>
    val usersState: StateFlow<List<UserEntity>>
    val transactionsState: StateFlow<List<FuelTransactionEntity>>
    val limitsState: StateFlow<List<FuelLimitEntity>>

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _selectedTabId = MutableStateFlow<Int?>(null)
    val selectedTabId: StateFlow<Int?> = _selectedTabId.asStateFlow()

    // Dynamically emits the rows corresponding to the active/selected tab
    val currentRowsState: StateFlow<List<RowEntity>> = _selectedTabId
        .flatMapLatest { tabId ->
            if (tabId != null) {
                repository.getRowsForTab(tabId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    // Tracks whether classified/confidential mode is unlocked/decrypted (viewable)
    private val _isConfidentialUnlocked = MutableStateFlow(false)
    val isConfidentialUnlocked: StateFlow<Boolean> = _isConfidentialUnlocked.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MilitaryAgendaRepository(
            tabDao = database.tabDao(),
            rowDao = database.rowDao(),
            chatMessageDao = database.chatMessageDao(),
            officerDao = database.officerDao(),
            statusOptionDao = database.statusOptionDao(),
            commTrafficDao = database.commTrafficDao(),
            userDao = database.userDao(),
            fuelTransactionDao = database.fuelTransactionDao(),
            fuelLimitDao = database.fuelLimitDao()
        )

        tabsState = repository.allTabs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        chatMessagesState = repository.allChatMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        officersState = repository.allOfficers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        statusOptionsState = repository.allStatusOptions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        commTrafficState = repository.allCommTraffic.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        usersState = repository.allUsers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactionsState = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        limitsState = repository.allLimits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.populateInitialData()
            // Select first tab by default when loaded
            tabsState.filter { it.isNotEmpty() }.firstOrNull()?.let { list ->
                if (_selectedTabId.value == null) {
                    _selectedTabId.value = list.first().id
                }
            }
        }
    }

    fun selectTab(tabId: Int) {
        _selectedTabId.value = tabId
    }

    fun setConfidentialUnlocked(unlocked: Boolean) {
        _isConfidentialUnlocked.value = unlocked
    }

    fun addCustomTab(name: String, columns: List<String>) {
        if (name.isBlank() || columns.isEmpty()) return
        viewModelScope.launch {
            val newTabId = repository.insertTab(
                TabEntity(name = name, columns = columns)
            ).toInt()
            _selectedTabId.value = newTabId
        }
    }

    fun deleteTab(tabId: Int) {
        viewModelScope.launch {
            val tabs = tabsState.value
            val tabToDelete = tabs.find { it.id == tabId }
            if (tabToDelete != null) {
                repository.deleteTab(tabToDelete)
                // Select another tab if the deleted one was selected
                if (_selectedTabId.value == tabId) {
                    val remaining = tabsState.value.filter { it.id != tabId }
                    _selectedTabId.value = remaining.firstOrNull()?.id
                }
            }
        }
    }

    fun addRowToSelectedTab(cells: List<String>, isConfidential: Boolean = false) {
        val tabId = _selectedTabId.value ?: return
        viewModelScope.launch {
            repository.insertRow(
                RowEntity(tabId = tabId, cells = cells, isConfidential = isConfidential)
            )
        }
    }

    fun updateRowValue(row: RowEntity, columnIndex: Int, newValue: String) {
        viewModelScope.launch {
            val updatedCells = row.cells.toMutableList()
            if (columnIndex < updatedCells.size) {
                updatedCells[columnIndex] = newValue
            } else {
                // Pad if list is smaller
                while (updatedCells.size <= columnIndex) {
                    updatedCells.add("")
                }
                updatedCells[columnIndex] = newValue
            }
            repository.updateRow(row.copy(cells = updatedCells))
        }
    }

    fun toggleRowConfidentiality(row: RowEntity) {
        viewModelScope.launch {
            repository.updateRow(row.copy(isConfidential = !row.isConfidential))
        }
    }

    fun deleteRow(row: RowEntity) {
        viewModelScope.launch {
            repository.deleteRow(row)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    fun askAssistant(query: String) {
        if (query.isBlank()) return
        _chatError.value = null
        viewModelScope.launch {
            // 1. Save user query locally
            repository.insertMessage(ChatMessageEntity(message = query, isUser = true))
            _isChatLoading.value = true

            try {
                // 2. Fetch full operational DB state for context
                val allTabs = tabsState.value
                val allRows = repository.getAllRowsSync()
                val officers = officersState.value
                val comms = commTrafficState.value
                val allTransactions = transactionsState.value
                val allLimits = limitsState.value

                // Mathematical calculations for personnel categories (Capacidad 1)
                val countVacaciones = officers.count { it.status.equals("Vacaciones", ignoreCase = true) || it.status.equals("Vacación", ignoreCase = true) }
                val countPermisoVacacional = officers.count { it.status.equals("Permiso Vacacional", ignoreCase = true) }
                val countPermisoEspecial = officers.count { it.status.equals("Permiso especial", ignoreCase = true) }
                val countReposoMedico = officers.count { it.status.equals("Reposo médico domiciliario", ignoreCase = true) }
                val countReposo = officers.count { it.status.equals("Reposo", ignoreCase = true) }
                val countGuardia = officers.count { it.status.equals("Guardia", ignoreCase = true) || it.status.equals("De guardia", ignoreCase = true) }

                val totalFalta = countVacaciones + countPermisoVacacional + countPermisoEspecial + countReposoMedico + countReposo + countGuardia
                val totalDisponibles = officers.size - totalFalta
                val totalPersonal = totalDisponibles + totalFalta

                val dbContextStr = buildString {
                    append("CONTEXTO OPERACIONAL GLOBAL (BASE NAVAL DE LA REDIMAIN, VENEZUELA):\n")
                    
                    append("\n1. PARTE DEL PERSONAL DE LA REH (ESTRUCTURA MATEMÁTICA Y DE CATEGORÍAS):\n")
                    append("  - TOTAL PERSONAL DE LA UNIDAD: $totalPersonal\n")
                    append("  - DISPONIBLES: $totalDisponibles (Personal sin condiciones operativas limitantes)\n")
                    append("  - FALTA: $totalFalta (Suma de personal en condiciones limitantes). Desglose exacto:\n")
                    append("    * Vacaciones: $countVacaciones\n")
                    append("    * Permiso Vacacional: $countPermisoVacacional\n")
                    append("    * Permiso Especial: $countPermisoEspecial\n")
                    append("    * Reposo Médico Domiciliario: $countReposoMedico\n")
                    append("    * Reposo: $countReposo\n")
                    append("    * Guardia: $countGuardia\n")
                    
                    append("\nListado detallado del Personal de Tripulantes / Expedientes Digitales:\n")
                    if (officers.isEmpty()) {
                        append("  - No hay tripulantes en el sistema actualmente.\n")
                    } else {
                        officers.forEach { off ->
                            append("  * Tripulante: ${off.name} | Cédula: ${off.cedula} | Teléfono: ${off.phone}\n")
                            append("    - Grado/Cargo: ${off.rankCargo} | Categoría: ${off.category} | Estatus: [${off.status}] del ${off.statusFromDate} al ${off.statusToDate}\n")
                            append("    - Domicilio: Habitación: \"${off.direccionHabitacion}\", Residencia actual en isla: \"${off.direccionResidencia}\"\n")
                            append("    - Núcleo Familiar: Cónyuge: \"${off.conyuge}\", Padres: \"${off.padres}\", Hijos: ${off.hijosJson}\n")
                            append("    - Datos Médicos: Tipo Sangre: ${off.grupoSanguineo}, Alergias: \"${off.alergias}\", Contacto de Emergencia: \"${off.contactoEmergencia}\"\n")
                            append("    - Tallas Vestuario (Intendencia): Camisa: ${off.tallaCamisa}, Guerrera: ${off.tallaGuerrera}, Botas: ${off.tallaBotas}, Gorra: ${off.tallaGorra}, Quepi: ${off.tallaQuepi}, Zapatos: ${off.tallaZapatos}, Pantalón: ${off.tallaPantalon}\n")
                        }
                    }

                    append("\n2. MISIONES Y AGENDA DE CONTROL:\n")
                    if (allTabs.isEmpty()) {
                        append("- No hay tablas o pestañas de control cargadas actualmente.\n")
                    } else {
                        allTabs.forEach { tab ->
                            append("\nPestaña de Control Agenda: \"${tab.name}\"\n")
                            append("Columnas: [${tab.columns.joinToString(", ")}]\n")
                            val tabRows = allRows.filter { it.tabId == tab.id }
                            append("Registros:\n")
                            if (tabRows.isEmpty()) {
                                append("  (Sin registros)\n")
                            } else {
                                tabRows.forEachIndexed { idx, row ->
                                    val confidentialText = if (row.isConfidential) " (DATOS CLASIFICADOS/CONFIDENCIAL)" else ""
                                    append("  - Fila ${idx + 1}: [${row.cells.joinToString(", ")}]$confidentialText\n")
                                }
                            }
                        }
                    }

                    append("\n3. TRÁFICO DE COMUNICACIONES (ÓRDENES ESCRITAS Y OFICIOS RECIBIDOS/REGISTRADOS):\n")
                    if (comms.isEmpty()) {
                        append("- No hay tráfico de órdenes cargadas en el sistema.\n")
                    } else {
                        comms.forEach { comm ->
                            append("  - Oficio N° ${comm.documentNumber} | Asunto: ${comm.subject} | Origen: ${comm.origin} | Ejecutor: ${comm.recipientRankName} | Recibido: ${comm.dateReceived} | Plazo: ${comm.deadlineDate} | Estatus: [${comm.status}]\n")
                        }
                    }

                    append("\n4. REGISTRO DE COMBUSTIBLE Y LOGÍSTICA:\n")
                    if (allTransactions.isEmpty()) {
                        append("- No hay transacciones de combustible registradas.\n")
                    } else {
                        append("Historial de Consumo e Inventario de Cargas:\n")
                        allTransactions.forEach { tx ->
                            append("  - ${tx.liters} Litros de ${tx.fuelType} suministrados a ${tx.personName} para ${tx.vehicleName} (Placa: ${tx.plate}) el ${tx.dateString}\n")
                        }
                    }
                    if (allLimits.isEmpty()) {
                        append("- No hay límites de combustible configurados.\n")
                    } else {
                        append("Cupos semanales configurados por beneficiario:\n")
                        allLimits.forEach { lim ->
                            append("  - Beneficiario: ${lim.plateOrPerson} | Cupo Semanal Asignado: ${lim.weeklyLimitLiters} Litros\n")
                        }
                    }
                }

                // 3. Build system instructions
                val systemPrompt = """
                    Eres el "Asistente Táctico Digital de Command", una inteligencia militar de élite al servicio directo del Almirante Comandante de la REDIMAIN de la Armada Bolivariana de Venezuela.
                    Tu tono de comunicación debe ser estrictamente formal, respetuoso, disciplinado y claro ("Entendido, mi Almirante", "Se reporta a su comando que...", etc.).
                    Tienes acceso instantáneo a toda la información de control y gestión militar del Comando REDIMAIN. Con esta información, debes responder con absoluta precisión y exactitud matemática.
                    
                    CAPACIDAD 1: ESTRUCTURA MATEMÁTICA Y DE CATEGORÍAS PARA EL PARTE DEL PERSONAL:
                    Siempre que el Almirante solicite un parte del personal (o pregunte por el estado del personal, cuántos están activos, subordinados, guardias, etc.), debes estructurar la respuesta de la siguiente forma precisa basada en las categorías reales de la REDIMAIN:
                    - **TOTAL PERSONAL**: (Suma de Disponibles + Falta)
                    - **DISPONIBLES**: (Personal sin condiciones operativas limitantes, como vacacionistas o enfermos)
                    - **FALTA**: (Suma de personal en las siguientes condiciones limitantes de fatiga/permiso/médicos) Desglose exacto:
                      * Vacaciones: mostrar la cuenta
                      * Permiso Vacacional: mostrar la cuenta
                      * Permiso Especial: mostrar la cuenta
                      * Reposo Médico Domiciliario: mostrar la cuenta
                      * Reposo: mostrar la cuenta
                      * Guardia: mostrar la cuenta

                    CAPACIDAD 2: AUDITORÍA DE MISIONES Y AGENDA:
                    Si se te solicita auditar o escanear la Agenda/Misiones/Pestañas o si detectas una pregunta sobre plazos:
                    - Audita proactivamente qué asuntos o misiones están pendientes o son un retraso (por su estado o fecha límite cercana, hoy siendo 2026-05-28).
                    - Lista las tareas que están por concluir pronto y aconseja acciones.

                    CAPACIDAD 3: MONITOREO DE TRÁFICO DE COMUNICACIONES:
                    - Revisa el estatus del flujo de correspondencia y comunicaciones. Si el Almirante pregunta por este flujo, advierte sobre oficios que llevan tiempo Pendientes.

                    CAPACIDAD 4: ANÁLISIS DE COMBUSTIBLE Y LOGÍSTICA:
                    - Posees acceso de lectura. Reporta niveles de suministro, consumo por vehículo, y asiste en la planificación de cupos semanales. Cruza datos de logística.

                    No inventes misiones de otros países ni nombres de oficiales ficticios, limítate única y estrictamente a los datos reales provistos en el contexto. Responde de forma sobria, militar, estructurada y muy clara.
                    
                    $dbContextStr
                """.trimIndent()

                // 4. Gather chat messages for history
                val chatHistory = chatMessagesState.value
                val contents = mutableListOf<Content>()
                
                // Add conversation turns
                // Keep last 6 messages to stay within prompt limits gracefully
                val historyToInclude = chatHistory.takeLast(6)
                historyToInclude.forEach { msg ->
                    contents.add(Content(parts = listOf(Part(text = msg.message))))
                }
                
                // If query wasn't added to history yet in state, add it.
                if (historyToInclude.none { it.message == query }) {
                    contents.add(Content(parts = listOf(Part(text = query))))
                }

                val request = GenerateContentRequest(
                    contents = contents,
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.4f)
                )

                // 5. Call API
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("API Key de Gemini no se encuentra configurada en AI Studio. Por favor, agregue GEMINI_API_KEY en su panel de Secretos.")
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Mi Almirante, no he podido procesar una respuesta táctica en estos momentos. Verifique la conexión con el comando central."

                // 6. Save answer locally
                repository.insertMessage(ChatMessageEntity(message = replyText, isUser = false))

            } catch (e: Exception) {
                // Add an error log message in chat history as feedback
                val errMsg = "Error del Sistema Táctico: ${e.localizedMessage ?: e.message}"
                _chatError.value = e.localizedMessage ?: e.message
                repository.insertMessage(ChatMessageEntity(message = "⚠️ Mi Almirante, se ha detectado una interrupción en el enlace satelital satélite Simón Bolívar: ${e.localizedMessage ?: e.message}", isUser = false))
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    // --- Officers CRUD ---
    fun addOfficer(
        name: String,
        cedula: String,
        phone: String,
        rankCargo: String,
        status: String,
        fromDate: String,
        toDate: String,
        hasAlarm: Boolean = false,
        photoUri: String? = null,
        category: String = "OFICIALES",
        direccionHabitacion: String = "",
        direccionResidencia: String = "",
        conyuge: String = "",
        padres: String = "",
        hijosJson: String = "[]",
        grupoSanguineo: String = "",
        alergias: String = "",
        contactoEmergencia: String = "",
        tallaCamisa: String = "",
        tallaGuerrera: String = "",
        tallaBotas: String = "",
        tallaGorra: String = "",
        tallaQuepi: String = "",
        tallaZapatos: String = "",
        tallaPantalon: String = ""
    ) {
        viewModelScope.launch {
            repository.insertOfficer(
                OfficerEntity(
                    name = name,
                    cedula = cedula,
                    phone = phone,
                    rankCargo = rankCargo,
                    status = status,
                    statusFromDate = fromDate,
                    statusToDate = toDate,
                    hasAlarm = hasAlarm,
                    photoUri = photoUri,
                    category = category,
                    direccionHabitacion = direccionHabitacion,
                    direccionResidencia = direccionResidencia,
                    conyuge = conyuge,
                    padres = padres,
                    hijosJson = hijosJson,
                    grupoSanguineo = grupoSanguineo,
                    alergias = alergias,
                    contactoEmergencia = contactoEmergencia,
                    tallaCamisa = tallaCamisa,
                    tallaGuerrera = tallaGuerrera,
                    tallaBotas = tallaBotas,
                    tallaGorra = tallaGorra,
                    tallaQuepi = tallaQuepi,
                    tallaZapatos = tallaZapatos,
                    tallaPantalon = tallaPantalon
                )
            )
        }
    }

    fun updateOfficer(officer: OfficerEntity) {
        viewModelScope.launch {
            repository.updateOfficer(officer)
        }
    }

    fun deleteOfficer(officer: OfficerEntity) {
        viewModelScope.launch {
            repository.deleteOfficer(officer)
        }
    }

    // --- Status Options CRUD ---
    fun addStatusOption(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertStatusOption(StatusOptionEntity(name = name))
        }
    }

    fun deleteStatusOption(option: StatusOptionEntity) {
        viewModelScope.launch {
            repository.deleteStatusOption(option)
        }
    }

    // --- Communications Traffic CRUD ---
    fun addCommTraffic(
        docNum: String,
        subject: String,
        origin: String,
        assignedTo: String,
        dateRec: String,
        deadline: String,
        hasAlarm: Boolean = false,
        imagePath: String? = null,
        status: String = "Pendiente"
    ) {
        viewModelScope.launch {
            repository.insertCommTraffic(
                CommTrafficEntity(
                    documentNumber = docNum,
                    subject = subject,
                    origin = origin,
                    recipientRankName = assignedTo,
                    dateReceived = dateRec,
                    deadlineDate = deadline,
                    hasDeadlineAlarm = hasAlarm,
                    imagePath = imagePath,
                    status = status
                )
            )
        }
    }

    fun updateCommTraffic(comm: CommTrafficEntity) {
        viewModelScope.launch {
            repository.updateCommTraffic(comm)
        }
    }

    fun deleteCommTraffic(comm: CommTrafficEntity) {
        viewModelScope.launch {
            repository.deleteCommTraffic(comm)
        }
    }

    // User/Login Operations
    fun loginWithGoogleEmail(email: String, onSuccess: (UserEntity) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                onSuccess(user)
            } else {
                onFailure("El usuario no existe. Por favor, regístrese primero.")
            }
        }
    }

    fun registerGoogleUser(email: String, name: String, onSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _currentUser.value = existing
                onSuccess(existing)
            } else {
                val newUser = UserEntity(email = email, name = name)
                repository.insertUser(newUser)
                val saved = repository.getUserByEmail(email) ?: newUser
                _currentUser.value = saved
                onSuccess(saved)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // Fuel Operations
    fun addFuelTransaction(
        personName: String,
        vehicleName: String,
        plate: String,
        liters: Double,
        fuelType: String,
        dateString: String
    ) {
        viewModelScopeScopeLaunch(personName, vehicleName, plate, liters, fuelType, dateString)
    }

    private fun viewModelScopeScopeLaunch(
        personName: String,
        vehicleName: String,
        plate: String,
        liters: Double,
        fuelType: String,
        dateString: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                FuelTransactionEntity(
                    personName = personName,
                    vehicleName = vehicleName,
                    plate = plate,
                    liters = liters,
                    fuelType = fuelType,
                    dateString = dateString
                )
            )
        }
    }

    fun deleteFuelTransaction(tx: FuelTransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun saveFuelLimit(plateOrPerson: String, limitLiters: Double) {
        viewModelScope.launch {
            repository.insertLimit(
                FuelLimitEntity(
                    plateOrPerson = plateOrPerson,
                    weeklyLimitLiters = limitLiters
                )
            )
        }
    }

    fun deleteFuelLimit(limit: FuelLimitEntity) {
        viewModelScope.launch {
            repository.deleteLimit(limit)
        }
    }

    fun sendVerificationCode(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    BackendRetrofitClient.service.sendOtp(SendOtpRequest(email))
                }
                if (response.success) {
                    onSuccess()
                } else {
                    onFailure(response.message ?: "Error al enviar el código.")
                }
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: e.message ?: "Error de red.")
            }
        }
    }

    fun verifyCode(email: String, pin: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    BackendRetrofitClient.service.verifyOtp(VerifyOtpRequest(email, pin))
                }
                if (response.success) {
                    onSuccess()
                } else {
                    onFailure(response.message ?: "Código de verificación inválido.")
                }
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: e.message ?: "Error de red.")
            }
        }
    }
}
