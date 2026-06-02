package com.example.data.database

import androidx.room.*
import com.example.data.model.TabEntity
import com.example.data.model.RowEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.OfficerEntity
import com.example.data.model.StatusOptionEntity
import com.example.data.model.CommTrafficEntity
import com.example.data.model.UserEntity
import com.example.data.model.FuelTransactionEntity
import com.example.data.model.FuelLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs ORDER BY timestamp ASC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Query("SELECT * FROM tabs WHERE id = :id")
    suspend fun getTabById(id: Int): TabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity): Long

    @Update
    suspend fun updateTab(tab: TabEntity)

    @Delete
    suspend fun deleteTab(tab: TabEntity)
}

@Dao
interface RowDao {
    @Query("SELECT * FROM rows WHERE tabId = :tabId ORDER BY timestamp ASC")
    fun getRowsForTab(tabId: Int): Flow<List<RowEntity>>

    @Query("SELECT * FROM rows WHERE tabId = :tabId ORDER BY timestamp ASC")
    suspend fun getRowsForTabSync(tabId: Int): List<RowEntity>

    @Query("SELECT * FROM rows ORDER BY timestamp ASC")
    suspend fun getAllRowsSync(): List<RowEntity>

    @Query("SELECT * FROM rows WHERE id = :id")
    suspend fun getRowById(id: Int): RowEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRow(row: RowEntity): Long

    @Update
    suspend fun updateRow(row: RowEntity)

    @Delete
    suspend fun deleteRow(row: RowEntity)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface OfficerDao {
    @Query("SELECT * FROM officers ORDER BY timestamp DESC")
    fun getAllOfficers(): Flow<List<OfficerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficer(officer: OfficerEntity): Long

    @Update
    suspend fun updateOfficer(officer: OfficerEntity)

    @Delete
    suspend fun deleteOfficer(officer: OfficerEntity)
}

@Dao
interface StatusOptionDao {
    @Query("SELECT * FROM status_options ORDER BY name ASC")
    fun getAllStatusOptions(): Flow<List<StatusOptionEntity>>

    @Query("SELECT COUNT(*) FROM status_options")
    suspend fun getStatusOptionsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusOption(option: StatusOptionEntity): Long

    @Delete
    suspend fun deleteStatusOption(option: StatusOptionEntity)
}

@Dao
interface CommTrafficDao {
    @Query("SELECT * FROM comm_traffic ORDER BY timestamp DESC")
    fun getAllCommTraffic(): Flow<List<CommTrafficEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommTraffic(comm: CommTrafficEntity): Long

    @Update
    suspend fun updateCommTraffic(comm: CommTrafficEntity)

    @Delete
    suspend fun deleteCommTraffic(comm: CommTrafficEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY registeredAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface FuelTransactionDao {
    @Query("SELECT * FROM fuel_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FuelTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FuelTransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: FuelTransactionEntity)
}

@Dao
interface FuelLimitDao {
    @Query("SELECT * FROM fuel_limits")
    fun getAllLimits(): Flow<List<FuelLimitEntity>>

    @Query("SELECT * FROM fuel_limits WHERE plateOrPerson = :key LIMIT 1")
    suspend fun getLimitByKey(key: String): FuelLimitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: FuelLimitEntity): Long

    @Delete
    suspend fun deleteLimit(limit: FuelLimitEntity)
}

