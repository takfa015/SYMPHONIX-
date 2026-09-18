package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {
    @Transaction
    @Query("SELECT * FROM cash_sessions ORDER BY dateMillis DESC, id DESC")
    fun getAllSessionsWithDetails(): Flow<List<SessionWithDetails>>

    @Transaction
    @Query("SELECT * FROM cash_sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionWithDetailsFlow(sessionId: Long): Flow<SessionWithDetails?>

    @Transaction
    @Query("SELECT * FROM cash_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionWithDetails(sessionId: Long): SessionWithDetails?

    @Query("SELECT * FROM cash_sessions ORDER BY dateMillis DESC, id DESC LIMIT 1")
    suspend fun getLatestSession(): CashSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CashSession): Long

    @Update
    suspend fun updateSession(session: CashSession)

    @Query("DELETE FROM cash_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    // Replenishments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplenishment(replenishment: CashReplenishment): Long

    @Query("DELETE FROM cash_replenishments WHERE id = :id")
    suspend fun deleteReplenishment(id: Long)

    @Query("SELECT COUNT(*) FROM cash_replenishments WHERE sessionId = :sessionId")
    suspend fun getReplenishmentCount(sessionId: Long): Int

    // Disbursements
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisbursement(disbursement: Disbursement): Long

    @Query("DELETE FROM disbursements WHERE id = :id")
    suspend fun deleteDisbursement(id: Long)

    @Query("SELECT COUNT(*) FROM disbursements WHERE sessionId = :sessionId")
    suspend fun getDisbursementCount(sessionId: Long): Int
}
