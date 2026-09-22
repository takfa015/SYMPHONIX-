package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.model.AuditEntry
import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement
import com.example.data.model.SessionWithDetails
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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
    val backupVersion: Int,
    val sessionsCount: Int,
    val replenishmentsCount: Int,
    val disbursementsCount: Int,
    val totalDisbursedCents: Long,
    val checksumVerified: Boolean
) {
    val totalDisbursed: Double get() = Money.centsToDouble(totalDisbursedCents)
}

data class BackupData(
    val summary: BackupSummary,
    val sessions: List<CashSession>,
    val replenishments: Map<Long, List<CashReplenishment>>, // mapped by original sessionId
    val disbursements: Map<Long, List<Disbursement>>,       // mapped by original sessionId
    val auditEntries: Map<Long, List<AuditEntry>> = emptyMap() // mapped by original sessionId
)

data class RestoreResult(
    val success: Boolean,
    val sessionsRestored: Int,
    val replenishmentsRestored: Int,
    val disbursementsRestored: Int,
    val message: String
)

object CashBackupManager {

    const val CURRENT_BACKUP_VERSION = 2
    const val APP_IDENTIFIER = "SYMPHONIX_CAISSE"

    /**
     * Purge les sauvegardes temporaires du cache applicatif au démarrage.
     */
    fun purgeTempCache(context: Context) {
        try {
            val cacheBackups = File(context.cacheDir, "backups")
            if (cacheBackups.exists()) {
                cacheBackups.listFiles()?.forEach { it.delete() }
            }
        } catch (_: Exception) {}
    }

    /**
     * Effectue une sauvegarde automatique de sécurité dans le stockage interne privé
     * avant toute opération de remplacement (mode REPLACE).
     */
    fun performAutoSafetyBackup(context: Context, sessions: List<SessionWithDetails>): File {
        val backupDir = File(context.filesDir, "safety_backups").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(backupDir, "auto_safety_backup_pre_replace_$dateStamp.json")
        val json = exportToJson(sessions)
        file.writeText(json, Charsets.UTF_8)
        return file
    }

    /**
     * Export des sessions au format JSON (version 2) avec checksum SHA-256.
     */
    fun exportToJson(sessionsWithDetails: List<SessionWithDetails>): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH)

        root.put("app", APP_IDENTIFIER)
        root.put("version", CURRENT_BACKUP_VERSION)
        root.put("exportedAtMillis", now)
        root.put("exportedAtText", dateFmt.format(Date(now)))
        root.put("sessionsCount", sessionsWithDetails.size)

        var totalReplenishments = 0
        var totalDisbursements = 0
        var grandDisbursementSumCents = 0L

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
            sObj.put("initialFund", s.initialFund) // En centimes (Long)
            sObj.put("initialFundTime", s.initialFundTime)
            sObj.put("initialFundSource", s.initialFundSource)
            sObj.put("countedCash", s.countedCash ?: JSONObject.NULL)
            sObj.put("closingTime", s.closingTime ?: JSONObject.NULL)
            sObj.put("isClosed", s.isClosed)
            sObj.put("reopenCount", s.reopenCount)
            sObj.put("integrityHash", s.integrityHash ?: JSONObject.NULL)
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
                rObj.put("amount", r.amount) // En centimes (Long)
                rObj.put("reason", r.reason)
                rObj.put("sourceLocation", r.sourceLocation)
                rObj.put("registeredBy", r.registeredBy)
                rObj.put("cancelledAt", r.cancelledAt ?: JSONObject.NULL)
                rObj.put("cancelReason", r.cancelReason ?: JSONObject.NULL)
                repArray.put(rObj)
            }
            sObj.put("replenishments", repArray)

            // Disbursements
            val disbArray = JSONArray()
            for (d in item.disbursements) {
                totalDisbursements++
                if (d.cancelledAt == null) {
                    grandDisbursementSumCents += d.amount
                }
                val dObj = JSONObject()
                dObj.put("id", d.id)
                dObj.put("orderNumber", d.orderNumber)
                dObj.put("time", d.time)
                dObj.put("designation", d.designation)
                dObj.put("parentCategory", d.parentCategory)
                dObj.put("subCategory", d.subCategory)
                dObj.put("amount", d.amount) // En centimes (Long)
                dObj.put("recipient", d.recipient)
                dObj.put("cancelledAt", d.cancelledAt ?: JSONObject.NULL)
                dObj.put("cancelReason", d.cancelReason ?: JSONObject.NULL)
                disbArray.put(dObj)
            }
            sObj.put("disbursements", disbArray)

            // Audit Entries
            val auditArray = JSONArray()
            for (a in item.auditEntries) {
                val aObj = JSONObject()
                aObj.put("id", a.id)
                aObj.put("timestamp", a.timestamp)
                aObj.put("action", a.action)
                aObj.put("entityType", a.entityType)
                aObj.put("entityId", a.entityId ?: JSONObject.NULL)
                aObj.put("detailsJson", a.detailsJson)
                aObj.put("reason", a.reason)
                auditArray.put(aObj)
            }
            sObj.put("auditEntries", auditArray)

            sessionsArray.put(sObj)
        }

        root.put("totalReplenishments", totalReplenishments)
        root.put("totalDisbursements", totalDisbursements)
        root.put("totalDisbursedAmountCents", grandDisbursementSumCents)
        root.put("sessions", sessionsArray)

        // Checksum SHA-256 calculé sur la représentation canonique des données
        val checksum = SessionIntegrity.sha256(sessionsArray.toString())
        root.put("checksum", checksum)

        return root.toString(2)
    }

    /**
     * Analyse et validation stricte du fichier de sauvegarde.
     * Prend en charge la version 2 (native centimes) et rétrocompatible version 1 (Double).
     */
    fun parseBackup(jsonString: String): BackupData {
        val root = try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Format de fichier invalide : le contenu n'est pas un document JSON valide.")
        }

        // Validation stricte de l'application
        val app = root.optString("app")
        if (app != APP_IDENTIFIER) {
            throw IllegalArgumentException("Ce fichier n'est pas une sauvegarde valide de l'application SYMPHONIX Caisse (identifiant '$app' inconnu).")
        }

        // Validation de la version
        val version = root.optInt("version", 1)
        if (version != 1 && version != 2) {
            throw IllegalArgumentException("Version de sauvegarde non supportée : $version. Versions admises : 1 ou 2.")
        }

        if (!root.has("sessions")) {
            throw IllegalArgumentException("Sauvegarde corrompue : la section 'sessions' est absente.")
        }

        val sessionsArray = root.getJSONArray("sessions")

        // Validation du checksum pour la version 2
        var checksumVerified = false
        if (version >= 2) {
            val fileChecksum = root.optString("checksum")
            if (fileChecksum.isBlank()) {
                throw IllegalArgumentException("Sauvegarde v2 invalide : la somme de contrôle (checksum) est absente.")
            }
            val computedChecksum = SessionIntegrity.sha256(sessionsArray.toString())
            if (fileChecksum != computedChecksum) {
                throw IllegalArgumentException("Intégrité compromise : la somme de contrôle (SHA-256) du fichier ne concorde pas. Le fichier a été altéré.")
            }
            checksumVerified = true
        }

        val exportedAt = root.optString("exportedAtText", "Date inconnue")
        val sessions = mutableListOf<CashSession>()
        val replenishmentsMap = mutableMapOf<Long, MutableList<CashReplenishment>>()
        val disbursementsMap = mutableMapOf<Long, MutableList<Disbursement>>()
        val auditMap = mutableMapOf<Long, MutableList<AuditEntry>>()

        var totalReps = 0
        var totalDisbs = 0
        var totalDisbSumCents = 0L

        for (i in 0 until sessionsArray.length()) {
            val sObj = sessionsArray.getJSONObject(i)
            val origSessionId = sObj.optLong("id", 0L)
            val reference = sObj.optString("reference").trim()
            if (reference.isBlank()) {
                throw IllegalArgumentException("Session #$i invalide : référence de session obligatoire manquante.")
            }

            // Gestion montants : v1 en Double, v2 en Long (centimes)
            val initialFundCents = if (version == 1) {
                val dbl = sObj.optDouble("initialFund", 0.0)
                if (dbl < 0.0 || dbl.isNaN() || dbl.isInfinite()) {
                    throw IllegalArgumentException("Session $reference : montant du fond initial invalide.")
                }
                Money.doubleToCents(dbl)
            } else {
                val cents = sObj.optLong("initialFund", 0L)
                if (cents < 0L) throw IllegalArgumentException("Session $reference : montant du fond initial négatif.")
                cents
            }

            val countedCashCents = if (!sObj.isNull("countedCash")) {
                if (version == 1) {
                    val dbl = sObj.getDouble("countedCash")
                    if (dbl < 0.0 || dbl.isNaN() || dbl.isInfinite()) {
                        throw IllegalArgumentException("Session $reference : comptage d'espèces invalide.")
                    }
                    Money.doubleToCents(dbl)
                } else {
                    val cents = sObj.getLong("countedCash")
                    if (cents < 0L) throw IllegalArgumentException("Session $reference : comptage d'espèces négatif.")
                    cents
                }
            } else null

            val session = CashSession(
                id = origSessionId,
                reference = reference,
                dateText = sObj.optString("dateText", ""),
                dateMillis = sObj.optLong("dateMillis", System.currentTimeMillis()),
                establishmentName = sObj.optString("establishmentName", "SYMPHONIX"),
                establishmentSubTitle = sObj.optString("establishmentSubTitle", "Finance & Gestion de Caisse"),
                responsibleName = sObj.optString("responsibleName", "Responsable de caisse"),
                managerName = sObj.optString("managerName", "Direction"),
                initialFund = initialFundCents,
                initialFundTime = sObj.optString("initialFundTime", "00:00"),
                initialFundSource = sObj.optString("initialFundSource", "Dotation de caisse"),
                countedCash = countedCashCents,
                closingTime = if (sObj.isNull("closingTime")) null else sObj.optString("closingTime"),
                isClosed = sObj.optBoolean("isClosed", false),
                reopenCount = sObj.optInt("reopenCount", 0),
                integrityHash = if (sObj.isNull("integrityHash")) null else sObj.optString("integrityHash"),
                currency = sObj.optString("currency", "DA"),
                notes = sObj.optString("notes", "")
            )
            sessions.add(session)

            // Alimentations
            val repList = mutableListOf<CashReplenishment>()
            if (sObj.has("replenishments")) {
                val repArray = sObj.getJSONArray("replenishments")
                for (j in 0 until repArray.length()) {
                    val rObj = repArray.getJSONObject(j)
                    val amountCents = if (version == 1) {
                        val dbl = rObj.optDouble("amount", 0.0)
                        if (dbl < 0.0 || dbl.isNaN() || dbl.isInfinite()) {
                            throw IllegalArgumentException("Alimentation invalide dans la session $reference.")
                        }
                        Money.doubleToCents(dbl)
                    } else {
                        val cents = rObj.optLong("amount", 0L)
                        if (cents < 0L) throw IllegalArgumentException("Alimentation négative dans la session $reference.")
                        cents
                    }

                    val rep = CashReplenishment(
                        id = rObj.optLong("id", 0L),
                        sessionId = origSessionId,
                        orderNumber = rObj.optInt("orderNumber", j + 1),
                        time = rObj.optString("time", "00:00"),
                        amount = amountCents,
                        reason = rObj.optString("reason", "Approvisionnement"),
                        sourceLocation = rObj.optString("sourceLocation", ""),
                        registeredBy = rObj.optString("registeredBy", ""),
                        cancelledAt = if (rObj.isNull("cancelledAt")) null else rObj.optLong("cancelledAt"),
                        cancelReason = if (rObj.isNull("cancelReason")) null else rObj.optString("cancelReason")
                    )
                    repList.add(rep)
                    totalReps++
                }
            }
            replenishmentsMap[origSessionId] = repList

            // Décaissements
            val disbList = mutableListOf<Disbursement>()
            if (sObj.has("disbursements")) {
                val disbArray = sObj.getJSONArray("disbursements")
                for (k in 0 until disbArray.length()) {
                    val dObj = disbArray.getJSONObject(k)
                    val designation = dObj.optString("designation", "").trim()
                    if (designation.isBlank()) {
                        throw IllegalArgumentException("Désignation obligatoire manquante pour un décaissement dans la session $reference.")
                    }

                    val amountCents = if (version == 1) {
                        val dbl = dObj.optDouble("amount", 0.0)
                        if (dbl < 0.0 || dbl.isNaN() || dbl.isInfinite()) {
                            throw IllegalArgumentException("Montant de décaissement invalide dans la session $reference.")
                        }
                        Money.doubleToCents(dbl)
                    } else {
                        val cents = dObj.optLong("amount", 0L)
                        if (cents < 0L) throw IllegalArgumentException("Montant de décaissement négatif dans la session $reference.")
                        cents
                    }

                    val cancelledAt = if (dObj.isNull("cancelledAt")) null else dObj.optLong("cancelledAt")
                    val disb = Disbursement(
                        id = dObj.optLong("id", 0L),
                        sessionId = origSessionId,
                        orderNumber = dObj.optInt("orderNumber", k + 1),
                        time = dObj.optString("time", "00:00"),
                        designation = designation,
                        parentCategory = dObj.optString("parentCategory", "Autre"),
                        subCategory = dObj.optString("subCategory", ""),
                        recipient = dObj.optString("recipient", ""),
                        amount = amountCents,
                        cancelledAt = cancelledAt,
                        cancelReason = if (dObj.isNull("cancelReason")) null else dObj.optString("cancelReason")
                    )
                    disbList.add(disb)
                    totalDisbs++
                    if (cancelledAt == null) {
                        totalDisbSumCents += amountCents
                    }
                }
            }
            disbursementsMap[origSessionId] = disbList

            // Audit
            val auditList = mutableListOf<AuditEntry>()
            if (sObj.has("auditEntries")) {
                val auditArray = sObj.getJSONArray("auditEntries")
                for (m in 0 until auditArray.length()) {
                    val aObj = auditArray.getJSONObject(m)
                    val entry = AuditEntry(
                        id = 0L,
                        sessionId = origSessionId,
                        timestamp = aObj.optLong("timestamp", System.currentTimeMillis()),
                        action = aObj.optString("action", "RESTORED"),
                        entityType = aObj.optString("entityType", "SESSION"),
                        entityId = if (aObj.isNull("entityId")) null else aObj.optLong("entityId"),
                        detailsJson = aObj.optString("detailsJson", "{}"),
                        reason = aObj.optString("reason", "")
                    )
                    auditList.add(entry)
                }
            }
            auditMap[origSessionId] = auditList
        }

        val summary = BackupSummary(
            exportedAt = exportedAt,
            backupVersion = version,
            sessionsCount = sessions.size,
            replenishmentsCount = totalReps,
            disbursementsCount = totalDisbs,
            totalDisbursedCents = totalDisbSumCents,
            checksumVerified = checksumVerified
        )

        return BackupData(
            summary = summary,
            sessions = sessions,
            replenishments = replenishmentsMap,
            disbursements = disbursementsMap,
            auditEntries = auditMap
        )
    }

    /**
     * Exécute la restauration en UNE TRANSACTION UNIQUE.
     * En cas d'échec ou d'exception, rollback automatique total.
     */
    suspend fun executeRestore(
        context: Context,
        db: AppDatabase,
        backupData: BackupData,
        mode: RestoreMode
    ): RestoreResult {
        return try {
            val dao = db.cashDao()

            // Sauvegarde de sécurité obligatoire avant écrasement
            if (mode == RestoreMode.REPLACE) {
                val existing = dao.getAllSessionsWithDetailsDirect()
                if (existing.isNotEmpty()) {
                    performAutoSafetyBackup(context, existing)
                }
            }

            var totalRepsRestored = 0
            var totalDisbsRestored = 0
            var totalSessionsRestored = 0

            db.withTransaction {
                if (mode == RestoreMode.REPLACE) {
                    dao.clearAllAuditEntries()
                    dao.clearAllDisbursements()
                    dao.clearAllReplenishments()
                    dao.clearAllSessions()
                }

                // Pour le mode MERGE : récupération des couples existants (reference, dateMillis)
                val existingKeys = if (mode == RestoreMode.MERGE) {
                    dao.getAllSessionsWithDetailsDirect().map {
                        it.session.reference to it.session.dateMillis
                    }.toSet()
                } else emptySet()

                for (session in backupData.sessions) {
                    val key = session.reference to session.dateMillis
                    if (mode == RestoreMode.MERGE && existingKeys.contains(key)) {
                        // Déduplication : ignorer les sessions déjà présentes
                        continue
                    }

                    val origId = session.id
                    val sessionToInsert = session.copy(id = 0L)
                    val newSessionId = dao.insertSession(sessionToInsert)
                    totalSessionsRestored++

                    // Lignes d'alimentations
                    val reps = backupData.replenishments[origId] ?: emptyList()
                    val remappedReps = reps.map {
                        it.copy(id = 0L, sessionId = newSessionId)
                    }
                    if (remappedReps.isNotEmpty()) {
                        dao.insertReplenishments(remappedReps)
                        totalRepsRestored += remappedReps.size
                    }

                    // Lignes de décaissements
                    val disbs = backupData.disbursements[origId] ?: emptyList()
                    val remappedDisbs = disbs.map {
                        it.copy(id = 0L, sessionId = newSessionId)
                    }
                    if (remappedDisbs.isNotEmpty()) {
                        dao.insertDisbursements(remappedDisbs)
                        totalDisbsRestored += remappedDisbs.size
                    }

                    // Entrées d'audit
                    val audits = backupData.auditEntries[origId] ?: emptyList()
                    val remappedAudits = audits.map {
                        it.copy(id = 0L, sessionId = newSessionId)
                    }
                    if (remappedAudits.isNotEmpty()) {
                        dao.insertAuditEntries(remappedAudits)
                    }

                    // Ajouter une entrée d'audit pour la restauration
                    dao.insertAuditEntry(
                        AuditEntry(
                            sessionId = newSessionId,
                            action = "RESTORED",
                            entityType = "SESSION",
                            entityId = newSessionId,
                            detailsJson = """{"mode":"$mode","backupVersion":${backupData.summary.backupVersion}}""",
                            reason = "Restauration des données depuis sauvegarde"
                        )
                    )
                }
            }

            RestoreResult(
                success = true,
                sessionsRestored = totalSessionsRestored,
                replenishmentsRestored = totalRepsRestored,
                disbursementsRestored = totalDisbsRestored,
                message = "Restauration terminée : $totalSessionsRestored session(s) et $totalDisbsRestored décaissement(s) intégrés avec succès."
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                sessionsRestored = 0,
                replenishmentsRestored = 0,
                disbursementsRestored = 0,
                message = "Échec de la restauration : ${e.localizedMessage ?: "Erreur inattendue"}"
            )
        }
    }

    /**
     * Crée l'intention de partage du fichier JSON de sauvegarde avec suppression après partage.
     */
    fun createShareIntent(context: Context, jsonString: String): Intent {
        val cacheDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
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
                "Fichier de sauvegarde sécurisé SYMPHONIX Caisse ($dateStamp). Contient l'historique complet et l'empreinte de contrôle SHA-256."
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
