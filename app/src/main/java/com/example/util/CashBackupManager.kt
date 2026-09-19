package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.CashDao
import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RestoreMode {
    MERGE,
    REPLACE
}

data class BackupSummary(
    val exportedAt: String,
    val sessionsCount: Int,
    val replenishmentsCount: Int,
    val disbursementsCount: Int,
    val totalDisbursed: Double
)

data class BackupData(
    val summary: BackupSummary,
    val sessions: List<CashSession>,
    val replenishments: Map<Long, List<CashReplenishment>>, // mapped by original sessionId
    val disbursements: Map<Long, List<Disbursement>>       // mapped by original sessionId
)

data class RestoreResult(
    val success: Boolean,
    val sessionsRestored: Int,
    val replenishmentsRestored: Int,
    val disbursementsRestored: Int,
    val message: String
)

object CashBackupManager {

    private const val BACKUP_VERSION = 1
    private const val APP_IDENTIFIER = "SYMPHONIX_CAISSE"

    fun exportToJson(sessionsWithDetails: List<SessionWithDetails>): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        root.put("app", APP_IDENTIFIER)
        root.put("version", BACKUP_VERSION)
        root.put("exportedAtMillis", now)
        root.put("exportedAtText", dateFmt.format(Date(now)))
        root.put("sessionsCount", sessionsWithDetails.size)

        var totalReplenishments = 0
        var totalDisbursements = 0
        var grandDisbursementSum = 0.0

        val sessionsArray = JSONArray()

        for (item in sessionsWithDetails) {
            val sObj = JSONObject()
            val s = item.session

            sObj.put("id", s.id)
            sObj.put("reference", s.reference)
            sObj.put("dateText", s.dateText)
            sObj.put("dateMillis", s.dateMillis)
            sObj.put("establishmentName", s.establishmentName)
            sObj.put("establishmentSubTitle", s.establishmentSubTitle)
            sObj.put("responsibleName", s.responsibleName)
            sObj.put("managerName", s.managerName)
            sObj.put("initialFund", s.initialFund)
            sObj.put("initialFundTime", s.initialFundTime)
            sObj.put("initialFundSource", s.initialFundSource)
            sObj.put("countedCash", s.countedCash ?: JSONObject.NULL)
            sObj.put("closingTime", s.closingTime ?: JSONObject.NULL)
            sObj.put("isClosed", s.isClosed)
            sObj.put("currency", s.currency)
            sObj.put("notes", s.notes)

            // Replenishments
            val repArray = JSONArray()
            for (r in item.replenishments) {
                totalReplenishments++
                val rObj = JSONObject()
                rObj.put("id", r.id)
                rObj.put("orderNumber", r.orderNumber)
                rObj.put("time", r.time)
                rObj.put("amount", r.amount)
                rObj.put("reason", r.reason)
                rObj.put("sourceLocation", r.sourceLocation)
                repArray.put(rObj)
            }
            sObj.put("replenishments", repArray)

            // Disbursements
            val disbArray = JSONArray()
            for (d in item.disbursements) {
                totalDisbursements++
                grandDisbursementSum += d.amount
                val dObj = JSONObject()
                dObj.put("id", d.id)
                dObj.put("orderNumber", d.orderNumber)
                dObj.put("time", d.time)
                dObj.put("designation", d.designation)
                dObj.put("parentCategory", d.parentCategory)
                dObj.put("subCategory", d.subCategory)
                dObj.put("recipient", d.recipient)
                dObj.put("amount", d.amount)
                disbArray.put(dObj)
            }
            sObj.put("disbursements", disbArray)

            sessionsArray.put(sObj)
        }

        root.put("totalReplenishments", totalReplenishments)
        root.put("totalDisbursements", totalDisbursements)
        root.put("totalDisbursedAmount", grandDisbursementSum)
        root.put("sessions", sessionsArray)

        return root.toString(2)
    }

    fun parseBackup(jsonString: String): BackupData {
        val root = JSONObject(jsonString)

        val exportedAt = root.optString("exportedAtText", "Date inconnue")
        val sessionsArray = root.getJSONArray("sessions")

        val sessions = mutableListOf<CashSession>()
        val replenishmentsMap = mutableMapOf<Long, MutableList<CashReplenishment>>()
        val disbursementsMap = mutableMapOf<Long, MutableList<Disbursement>>()

        var totalReps = 0
        var totalDisbs = 0
        var totalDisbSum = 0.0

        for (i in 0 until sessionsArray.length()) {
            val sObj = sessionsArray.getJSONObject(i)
            val origSessionId = sObj.getLong("id")

            val session = CashSession(
                id = origSessionId,
                reference = sObj.optString("reference", "REC-SESSION-$origSessionId"),
                dateText = sObj.optString("dateText", ""),
                dateMillis = sObj.optLong("dateMillis", System.currentTimeMillis()),
                establishmentName = sObj.optString("establishmentName", "SYMPHONIX"),
                establishmentSubTitle = sObj.optString("establishmentSubTitle", "Finance & Gestion de Caisse"),
                responsibleName = sObj.optString("responsibleName", "Responsable de caisse"),
                managerName = sObj.optString("managerName", "Direction"),
                initialFund = sObj.optDouble("initialFund", 0.0),
                initialFundTime = sObj.optString("initialFundTime", "00:00"),
                initialFundSource = sObj.optString("initialFundSource", "Dotation de caisse"),
                countedCash = if (sObj.isNull("countedCash")) null else sObj.optDouble("countedCash"),
                closingTime = if (sObj.isNull("closingTime")) null else sObj.optString("closingTime"),
                isClosed = sObj.optBoolean("isClosed", false),
                currency = sObj.optString("currency", "DA"),
                notes = sObj.optString("notes", "")
            )
            sessions.add(session)

            // Parse replenishments
            val repList = mutableListOf<CashReplenishment>()
            if (sObj.has("replenishments")) {
                val repArray = sObj.getJSONArray("replenishments")
                for (j in 0 until repArray.length()) {
                    val rObj = repArray.getJSONObject(j)
                    val rep = CashReplenishment(
                        id = rObj.optLong("id", 0L),
                        sessionId = origSessionId,
                        orderNumber = rObj.optInt("orderNumber", j + 1),
                        time = rObj.optString("time", "00:00"),
                        amount = rObj.optDouble("amount", 0.0),
                        reason = rObj.optString("reason", "Approvisionnement"),
                        sourceLocation = rObj.optString("sourceLocation", "")
                    )
                    repList.add(rep)
                    totalReps++
                }
            }
            replenishmentsMap[origSessionId] = repList

            // Parse disbursements
            val disbList = mutableListOf<Disbursement>()
            if (sObj.has("disbursements")) {
                val disbArray = sObj.getJSONArray("disbursements")
                for (k in 0 until disbArray.length()) {
                    val dObj = disbArray.getJSONObject(k)
                    val amount = dObj.optDouble("amount", 0.0)
                    val disb = Disbursement(
                        id = dObj.optLong("id", 0L),
                        sessionId = origSessionId,
                        orderNumber = dObj.optInt("orderNumber", k + 1),
                        time = dObj.optString("time", "00:00"),
                        designation = dObj.optString("designation", "Dépense"),
                        parentCategory = dObj.optString("parentCategory", "Autre"),
                        subCategory = dObj.optString("subCategory", ""),
                        recipient = dObj.optString("recipient", ""),
                        amount = amount
                    )
                    disbList.add(disb)
                    totalDisbs++
                    totalDisbSum += amount
                }
            }
            disbursementsMap[origSessionId] = disbList
        }

        val summary = BackupSummary(
            exportedAt = exportedAt,
            sessionsCount = sessions.size,
            replenishmentsCount = totalReps,
            disbursementsCount = totalDisbs,
            totalDisbursed = totalDisbSum
        )

        return BackupData(
            summary = summary,
            sessions = sessions,
            replenishments = replenishmentsMap,
            disbursements = disbursementsMap
        )
    }

    suspend fun executeRestore(
        dao: CashDao,
        backupData: BackupData,
        mode: RestoreMode
    ): RestoreResult {
        return try {
            if (mode == RestoreMode.REPLACE) {
                dao.clearAllDisbursements()
                dao.clearAllReplenishments()
                dao.clearAllSessions()
            }

            var totalRepsRestored = 0
            var totalDisbsRestored = 0

            for (session in backupData.sessions) {
                val origId = session.id
                // In merge mode, insert session with id = 0 to generate a fresh ID if needed
                val sessionToInsert = if (mode == RestoreMode.MERGE) {
                    session.copy(id = 0)
                } else {
                    session
                }

                val newSessionId = dao.insertSession(sessionToInsert)

                val reps = backupData.replenishments[origId] ?: emptyList()
                val remappedReps = reps.map {
                    it.copy(id = if (mode == RestoreMode.MERGE) 0L else it.id, sessionId = newSessionId)
                }
                if (remappedReps.isNotEmpty()) {
                    dao.insertReplenishments(remappedReps)
                    totalRepsRestored += remappedReps.size
                }

                val disbs = backupData.disbursements[origId] ?: emptyList()
                val remappedDisbs = disbs.map {
                    it.copy(id = if (mode == RestoreMode.MERGE) 0L else it.id, sessionId = newSessionId)
                }
                if (remappedDisbs.isNotEmpty()) {
                    dao.insertDisbursements(remappedDisbs)
                    totalDisbsRestored += remappedDisbs.size
                }
            }

            RestoreResult(
                success = true,
                sessionsRestored = backupData.sessions.size,
                replenishmentsRestored = totalRepsRestored,
                disbursementsRestored = totalDisbsRestored,
                message = "Restauration terminée : ${backupData.sessions.size} session(s) et $totalDisbsRestored décaissement(s) récupéré(s)."
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                sessionsRestored = 0,
                replenishmentsRestored = 0,
                disbursementsRestored = 0,
                message = "Échec de la restauration : ${e.localizedMessage}"
            )
        }
    }

    fun createShareIntent(context: Context, jsonString: String): Intent {
        val cacheDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "symphonix_caisse_backup_$dateStamp.json"
        val file = File(cacheDir, fileName)

        file.writeText(jsonString, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Sauvegarde SYMPHONIX Caisse - $dateStamp")
            putExtra(
                Intent.EXTRA_TEXT,
                "Fichier de sauvegarde sécurisé SYMPHONIX Caisse ($dateStamp). Gardez ce fichier pour restaurer vos données à tout moment."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun writeToUri(context: Context, uri: Uri, jsonString: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonString.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
