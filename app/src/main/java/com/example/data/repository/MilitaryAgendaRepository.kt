package com.example.data.repository

import com.example.data.database.TabDao
import com.example.data.database.RowDao
import com.example.data.database.ChatMessageDao
import com.example.data.model.TabEntity
import com.example.data.model.RowEntity
import com.example.data.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.example.data.database.OfficerDao
import com.example.data.database.StatusOptionDao
import com.example.data.database.CommTrafficDao
import com.example.data.database.UserDao
import com.example.data.database.FuelTransactionDao
import com.example.data.database.FuelLimitDao
import com.example.data.model.OfficerEntity
import com.example.data.model.StatusOptionEntity
import com.example.data.model.CommTrafficEntity
import com.example.data.model.UserEntity
import com.example.data.model.FuelTransactionEntity
import com.example.data.model.FuelLimitEntity

class MilitaryAgendaRepository(
    private val tabDao: TabDao,
    private val rowDao: RowDao,
    private val chatMessageDao: ChatMessageDao,
    private val officerDao: OfficerDao,
    private val statusOptionDao: StatusOptionDao,
    private val commTrafficDao: CommTrafficDao,
    private val userDao: UserDao,
    private val fuelTransactionDao: FuelTransactionDao,
    private val fuelLimitDao: FuelLimitDao
) {
    val allTabs: Flow<List<TabEntity>> = tabDao.getAllTabs()
    val allChatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()
    val allOfficers: Flow<List<OfficerEntity>> = officerDao.getAllOfficers()
    val allStatusOptions: Flow<List<StatusOptionEntity>> = statusOptionDao.getAllStatusOptions()
    val allCommTraffic: Flow<List<CommTrafficEntity>> = commTrafficDao.getAllCommTraffic()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allTransactions: Flow<List<FuelTransactionEntity>> = fuelTransactionDao.getAllTransactions()
    val allLimits: Flow<List<FuelLimitEntity>> = fuelLimitDao.getAllLimits()

    fun getRowsForTab(tabId: Int): Flow<List<RowEntity>> = 
        rowDao.getRowsForTab(tabId).map { list -> list.map { decryptRow(it) } }

    suspend fun getTabById(id: Int): TabEntity? = tabDao.getTabById(id)
    
    suspend fun getRowsForTabSync(tabId: Int): List<RowEntity> = 
        rowDao.getRowsForTabSync(tabId).map { decryptRow(it) }
        
    suspend fun getAllRowsSync(): List<RowEntity> = 
        rowDao.getAllRowsSync().map { decryptRow(it) }

    suspend fun insertTab(tab: TabEntity): Long = tabDao.insertTab(tab)
    suspend fun updateTab(tab: TabEntity) = tabDao.updateTab(tab)
    suspend fun deleteTab(tab: TabEntity) = tabDao.deleteTab(tab)

    suspend fun insertRow(row: RowEntity): Long = rowDao.insertRow(encryptRow(row))
    suspend fun updateRow(row: RowEntity) = rowDao.updateRow(encryptRow(row))
    suspend fun deleteRow(row: RowEntity) = rowDao.deleteRow(row)

    suspend fun insertMessage(msg: ChatMessageEntity): Long = chatMessageDao.insertMessage(msg)
    suspend fun clearChatHistory() = chatMessageDao.clearHistory()

    // Officers DB operations
    suspend fun insertOfficer(officer: OfficerEntity): Long = officerDao.insertOfficer(officer)
    suspend fun updateOfficer(officer: OfficerEntity) = officerDao.updateOfficer(officer)
    suspend fun deleteOfficer(officer: OfficerEntity) = officerDao.deleteOfficer(officer)

    // Status option DB operations
    suspend fun insertStatusOption(option: StatusOptionEntity): Long = statusOptionDao.insertStatusOption(option)
    suspend fun deleteStatusOption(option: StatusOptionEntity) = statusOptionDao.deleteStatusOption(option)

    // Communication traffic DB operations
    suspend fun insertCommTraffic(comm: CommTrafficEntity): Long = commTrafficDao.insertCommTraffic(comm)
    suspend fun updateCommTraffic(comm: CommTrafficEntity) = commTrafficDao.updateCommTraffic(comm)
    suspend fun deleteCommTraffic(comm: CommTrafficEntity) = commTrafficDao.deleteCommTraffic(comm)

    // User CRUD
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    // Fuel supply CRUD
    suspend fun insertTransaction(tx: FuelTransactionEntity): Long = fuelTransactionDao.insertTransaction(tx)
    suspend fun deleteTransaction(tx: FuelTransactionEntity) = fuelTransactionDao.deleteTransaction(tx)

    // Fuel limits CRUD
    suspend fun insertLimit(limit: FuelLimitEntity): Long = fuelLimitDao.insertLimit(limit)
    suspend fun getLimitByKey(key: String): FuelLimitEntity? = fuelLimitDao.getLimitByKey(key)
    suspend fun deleteLimit(limit: FuelLimitEntity) = fuelLimitDao.deleteLimit(limit)

    suspend fun populateInitialData() {
        // Prepopulate default statuses if empty
        val statusCount = statusOptionDao.getStatusOptionsCount()
        if (statusCount == 0) {
            SampleData.defaultStatusOptions.forEach { statusOptionDao.insertStatusOption(it) }
        }

        // Prepopulate Google registered user (admin/demo)
        val defaultUser = userDao.getUserByEmail(SampleData.defaultUser.email)
        if (defaultUser == null) {
            userDao.insertUser(SampleData.defaultUser)
        }

        // Prepopulate starting fuel limits (configured weekly limit per plate/person)
        SampleData.defaultFuelLimits.forEach { limit ->
            if (fuelLimitDao.getLimitByKey(limit.plateOrPerson) == null) {
                fuelLimitDao.insertLimit(limit)
            }
        }

        // Insert sample fuel supplies with fixed IDs to prevent duplication on multiple runs
        SampleData.defaultFuelTransactions.forEach { tx ->
            fuelTransactionDao.insertTransaction(tx)
        }

        // Prepopulate sample officer crew if empty
        // Tab generation logic
        val currentTabs = tabDao.getTabById(1)
        if (currentTabs == null) {
            // Create Dynamic Tabs and Rows
            SampleData.defaultTabs.forEach { tab ->
                val tabId = tabDao.insertTab(tab).toInt()
                SampleData.defaultRows[tab.id]?.forEach { row ->
                    insertRow(row.copy(tabId = tabId))
                }
            }

            // Insert default Officers
            SampleData.defaultOfficers.forEach { officerDao.insertOfficer(it) }

            // Insert default written communication notes
            SampleData.defaultCommTraffic.forEach { commTrafficDao.insertCommTraffic(it) }
        }
    }

    private val ENCRYPTION_KEY = "REDIMAIN-TACTICAL-KEY"

    private fun encryptRow(row: RowEntity): RowEntity {
        if (!row.isConfidential) return row
        val encryptedCells = row.cells.map { cell ->
            encryptString(cell)
        }
        return row.copy(cells = encryptedCells)
    }

    private fun decryptRow(row: RowEntity): RowEntity {
        if (!row.isConfidential) return row
        val decryptedCells = row.cells.map { cell ->
            decryptString(cell)
        }
        return row.copy(cells = decryptedCells)
    }

    private fun encryptString(input: String): String {
        if (input.isBlank()) return ""
        val bytes = input.toByteArray(Charsets.UTF_8)
        val encrypted = ByteArray(bytes.size)
        val keyBytes = ENCRYPTION_KEY.toByteArray(Charsets.UTF_8)
        for (i in bytes.indices) {
            encrypted[i] = (bytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
    }

    private fun decryptString(input: String): String {
        if (input.isBlank()) return ""
        return try {
            val bytes = android.util.Base64.decode(input, android.util.Base64.NO_WRAP)
            val decrypted = ByteArray(bytes.size)
            val keyBytes = ENCRYPTION_KEY.toByteArray(Charsets.UTF_8)
            for (i in bytes.indices) {
                decrypted[i] = (bytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            input
        }
    }
}
