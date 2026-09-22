package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AuditEntry
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

    // ABORT on conflict to avoid cascading deletion of children
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: CashSession): Long

    @Update
    suspend fun updateSession(session: CashSession)

    @Query("DELETE FROM cash_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    // Replenishments
    @Query("SELECT COALESCE(MAX(orderNumber), 0) + 1 FROM cash_replenishments WHERE sessionId = :sessionId")
    suspend fun getNextReplenishmentOrderNumber(sessionId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReplenishment(replenishment: CashReplenishment): Long

    @Query("SELECT * FROM cash_replenishments WHERE id = :id LIMIT 1")
    suspend fun getReplenishmentById(id: Long): CashReplenishment?

    @Query("UPDATE cash_replenishments SET cancelledAt = :cancelledAt, cancelReason = :reason WHERE id = :id")
    suspend fun cancelReplenishment(id: Long, cancelledAt: Long, reason: String)

    @Query("DELETE FROM cash_replenishments WHERE id = :id")
    suspend fun deleteReplenishmentPhysically(id: Long)

    @Query("SELECT COUNT(*) FROM cash_replenishments WHERE sessionId = :sessionId AND cancelledAt IS NULL")
    suspend fun getActiveReplenishmentCount(sessionId: Long): Int

    // Disbursements
    @Query("SELECT COALESCE(MAX(orderNumber), 0) + 1 FROM disbursements WHERE sessionId = :sessionId")
    suspend fun getNextDisbursementOrderNumber(sessionId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDisbursement(disbursement: Disbursement): Long

    @Query("SELECT * FROM disbursements WHERE id = :id LIMIT 1")
    suspend fun getDisbursementById(id: Long): Disbursement?

    @Query("UPDATE disbursements SET cancelledAt = :cancelledAt, cancelReason = :reason WHERE id = :id")
    suspend fun cancelDisbursement(id: Long, cancelledAt: Long, reason: String)

    @Query("DELETE FROM disbursements WHERE id = :id")
    suspend fun deleteDisbursementPhysically(id: Long)

    @Query("SELECT COUNT(*) FROM disbursements WHERE sessionId = :sessionId AND cancelledAt IS NULL")
    suspend fun getActiveDisbursementCount(sessionId: Long): Int

    // Audit entries
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditEntry(entry: AuditEntry): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditEntries(entries: List<AuditEntry>)

    @Query("SELECT * FROM audit_entries WHERE sessionId = :sessionId ORDER BY timestamp DESC, id DESC")
    fun getAuditEntriesFlow(sessionId: Long): Flow<List<AuditEntry>>

    // Backup & Restore
    @Transaction
    @Query("SELECT * FROM cash_sessions ORDER BY dateMillis ASC, id ASC")
    suspend fun getAllSessionsWithDetailsDirect(): List<SessionWithDetails>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReplenishments(replenishments: List<CashReplenishment>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDisbursements(disbursements: List<Disbursement>)

    @Query("DELETE FROM audit_entries")
    suspend fun clearAllAuditEntries()

    @Query("DELETE FROM cash_replenishments")
    suspend fun clearAllReplenishments()

    @Query("DELETE FROM disbursements")
    suspend fun clearAllDisbursements()

    @Query("DELETE FROM cash_sessions")
    suspend fun clearAllSessions()
}
