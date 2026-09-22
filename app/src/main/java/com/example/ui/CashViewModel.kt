package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AuditEntry
import com.example.data.model.SessionWithDetails
import com.example.data.repository.CashRepository
import com.example.util.BackupData
import com.example.util.CashBackupManager
import com.example.util.CashPdfGenerator
import com.example.util.Money
import com.example.util.RestoreMode
import com.example.util.RestoreResult
import com.example.util.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class SharePdf(val file: File, val title: String) : UiEvent
}

class CashViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository: CashRepository = CashRepository(db)

    init {
        // Purge des fichiers de sauvegarde temporaires du cache au démarrage
        CashBackupManager.purgeTempCache(application)
    }

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    val selectedSessionId: StateFlow<Long?> = _selectedSessionId.asStateFlow()

    val allSessions: StateFlow<List<SessionWithDetails>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentSession: StateFlow<SessionWithDetails?> = combine(
        repository.allSessions,
        _selectedSessionId
    ) { sessions, selectedId ->
        if (selectedId != null) {
            sessions.find { it.session.id == selectedId } ?: sessions.firstOrNull()
        } else {
            sessions.firstOrNull()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentSessionAuditEntries: StateFlow<List<AuditEntry>> = _selectedSessionId
        .flatMapLatest { sessionId ->
            val id = sessionId ?: currentSession.value?.session?.id
            if (id != null) {
                repository.cashDao.getAuditEntriesFlow(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun selectSession(sessionId: Long) {
        _selectedSessionId.value = sessionId
    }

    fun addReplenishment(
        amountCents: Long,
        reason: String,
        sourceLocation: String,
        time: String = "",
        registeredBy: String = ""
    ) {
        val session = currentSession.value?.session ?: return
        if (session.isClosed) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Session clôturée : ajout impossible"))
            }
            return
        }

        val effectiveTime = if (time.isNotBlank()) time else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                repository.addReplenishment(
                    sessionId = session.id,
                    amountCents = amountCents,
                    time = effectiveTime,
                    reason = reason,
                    sourceLocation = sourceLocation,
                    registeredBy = registeredBy.ifBlank { session.responsibleName }
                )
                _uiEvent.emit(UiEvent.ShowToast("Alimentation de ${Money.format(amountCents, session.currency)} enregistrée"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur : ${e.localizedMessage}"))
            }
        }
    }

    fun addReplenishment(
        amount: Double,
        reason: String,
        sourceLocation: String,
        time: String = "",
        registeredBy: String = ""
    ) {
        addReplenishment(Money.doubleToCents(amount), reason, sourceLocation, time, registeredBy)
    }

    /**
     * Annulation douce d'une alimentation avec motif journalisé (pas de suppression physique).
     */
    fun cancelReplenishment(id: Long, reason: String = "Annulation demandée par l'utilisateur") {
        val session = currentSession.value?.session ?: return
        if (session.isClosed) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Session clôturée : modification impossible"))
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.cancelReplenishment(session.id, id, reason)
                _uiEvent.emit(UiEvent.ShowToast("Ligne d'alimentation annulée"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur : ${e.localizedMessage}"))
            }
        }
    }

    // Rétrocompatibilité d'appel
    fun deleteReplenishment(id: Long) {
        cancelReplenishment(id, "Suppression demandée par l'opérateur")
    }

    fun addDisbursement(
        amountCents: Long,
        designation: String,
        parentCategory: String,
        subCategory: String,
        recipient: String = "",
        time: String = ""
    ) {
        val session = currentSession.value?.session ?: return
        if (session.isClosed) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Session clôturée : ajout impossible"))
            }
            return
        }

        val effectiveTime = if (time.isNotBlank()) time else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                repository.addDisbursement(
                    sessionId = session.id,
                    amountCents = amountCents,
                    time = effectiveTime,
                    designation = designation,
                    parentCategory = parentCategory,
                    subCategory = subCategory,
                    recipient = recipient
                )
                _uiEvent.emit(UiEvent.ShowToast("Décaissement de ${Money.format(amountCents, session.currency)} enregistré"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur : ${e.localizedMessage}"))
            }
        }
    }

    fun addDisbursement(
        amount: Double,
        designation: String,
        parentCategory: String,
        subCategory: String,
        recipient: String = "",
        time: String = ""
    ) {
        addDisbursement(
            amountCents = Money.doubleToCents(amount),
            designation = designation,
            parentCategory = parentCategory,
            subCategory = subCategory,
            recipient = recipient,
            time = time
        )
    }

    /**
     * Annulation douce d'un décaissement avec motif journalisé (pas de suppression physique).
     */
    fun cancelDisbursement(id: Long, reason: String = "Annulation demandée par l'utilisateur") {
        val session = currentSession.value?.session ?: return
        if (session.isClosed) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Session clôturée : modification impossible"))
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.cancelDisbursement(session.id, id, reason)
                _uiEvent.emit(UiEvent.ShowToast("Ligne de décaissement annulée"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur : ${e.localizedMessage}"))
            }
        }
    }

    // Rétrocompatibilité d'appel
    fun deleteDisbursement(id: Long) {
        cancelDisbursement(id, "Suppression demandée par l'opérateur")
    }

    fun closeSession(countedCashCents: Long, closingTime: String = "") {
        val session = currentSession.value?.session ?: return
        val effectiveTime = if (closingTime.isNotBlank()) closingTime else SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                repository.closeSession(
                    sessionId = session.id,
                    countedCashCents = countedCashCents,
                    closingTime = effectiveTime
                )
                _uiEvent.emit(UiEvent.ShowToast("Caisse clôturée avec empreinte d'intégrité à $effectiveTime"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur lors de la clôture : ${e.localizedMessage}"))
            }
        }
    }

    fun closeSession(countedCash: Double, closingTime: String = "") {
        closeSession(Money.doubleToCents(countedCash), closingTime)
    }

    /**
     * Réouverture de session avec vérification PIN (si configuré) et motif obligatoire.
     */
    fun reopenSession(
        reason: String = "Réouverture pour rectifications",
        inputPin: String? = null,
        context: Context = getApplication(),
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val session = currentSession.value?.session ?: return
        if (reason.isBlank()) {
            val err = "Un motif de réouverture est obligatoire."
            onError(err)
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowToast(err)) }
            return
        }

        if (SecurityManager.isSecurityEnabled(context)) {
            if (inputPin.isNullOrBlank() || !SecurityManager.verifyPin(context, inputPin)) {
                val err = "Code PIN incorrect."
                onError(err)
                viewModelScope.launch { _uiEvent.emit(UiEvent.ShowToast(err)) }
                return
            }
        }

        viewModelScope.launch {
            try {
                repository.reopenSession(session.id, reason)
                _uiEvent.emit(UiEvent.ShowToast("Session rouverte pour rectifications"))
                onSuccess()
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Erreur inconnue"
                onError(msg)
                _uiEvent.emit(UiEvent.ShowToast("Erreur : $msg"))
            }
        }
    }

    fun createNewSession(
        initialFundCents: Long,
        initialFundSource: String = "Dotation de caisse",
        establishmentName: String = "Établissement commercial",
        establishmentSubTitle: String = "",
        responsibleName: String = "Responsable de caisse",
        managerName: String = "",
        currency: String = "DA"
    ) {
        val time = SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            try {
                val newId = repository.createNewSession(
                    initialFundCents = initialFundCents,
                    initialFundTime = time,
                    initialFundSource = initialFundSource,
                    establishmentName = establishmentName,
                    establishmentSubTitle = establishmentSubTitle,
                    responsibleName = responsibleName,
                    managerName = managerName,
                    currency = currency
                )
                _selectedSessionId.value = newId
                _uiEvent.emit(UiEvent.ShowToast("Nouvelle session de caisse ouverte !"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur création : ${e.localizedMessage}"))
            }
        }
    }

    fun createNewSession(
        initialFund: Double,
        initialFundSource: String = "Dotation de caisse",
        establishmentName: String = "Établissement commercial",
        establishmentSubTitle: String = "",
        responsibleName: String = "Responsable de caisse",
        managerName: String = "",
        currency: String = "DA"
    ) {
        createNewSession(
            initialFundCents = Money.doubleToCents(initialFund),
            initialFundSource = initialFundSource,
            establishmentName = establishmentName,
            establishmentSubTitle = establishmentSubTitle,
            responsibleName = responsibleName,
            managerName = managerName,
            currency = currency
        )
    }

    fun loadDemoData() {
        viewModelScope.launch {
            try {
                val id = repository.loadDemoData()
                _selectedSessionId.value = id
                _uiEvent.emit(UiEvent.ShowToast("Données de démonstration chargées"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur démo : ${e.localizedMessage}"))
            }
        }
    }

    fun exportAndSharePdf() {
        val details = currentSession.value ?: return
        exportAndSharePdfForSession(details)
    }

    fun exportAndSharePdfForSession(details: SessionWithDetails) {
        viewModelScope.launch {
            try {
                val file = CashPdfGenerator.generatePdfFile(getApplication(), details)
                _uiEvent.emit(
                    UiEvent.SharePdf(
                        file = file,
                        title = "Récapitulatif Caisse ${details.session.reference}"
                    )
                )
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur de génération PDF : ${e.localizedMessage}"))
            }
        }
    }

    fun updateSettings(
        establishmentName: String,
        establishmentSubTitle: String,
        responsibleName: String,
        managerName: String,
        currency: String,
        initialFundCents: Long
    ) {
        val session = currentSession.value?.session ?: return
        viewModelScope.launch {
            try {
                repository.updateSessionSettings(
                    sessionId = session.id,
                    establishmentName = establishmentName.trim(),
                    establishmentSubTitle = establishmentSubTitle.trim(),
                    responsibleName = responsibleName.trim(),
                    managerName = managerName.trim(),
                    currency = currency.trim(),
                    initialFundCents = initialFundCents
                )
                _uiEvent.emit(UiEvent.ShowToast("Paramètres enregistrés avec succès"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur mise à jour : ${e.localizedMessage}"))
            }
        }
    }

    fun updateSettings(
        establishmentName: String,
        establishmentSubTitle: String,
        responsibleName: String,
        managerName: String,
        currency: String,
        initialFund: Double
    ) {
        updateSettings(
            establishmentName = establishmentName,
            establishmentSubTitle = establishmentSubTitle,
            responsibleName = responsibleName,
            managerName = managerName,
            currency = currency,
            initialFundCents = Money.doubleToCents(initialFund)
        )
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteSession(sessionId)
                if (_selectedSessionId.value == sessionId) {
                    _selectedSessionId.value = null
                }
                _uiEvent.emit(UiEvent.ShowToast("Session supprimée"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur suppression : ${e.localizedMessage}"))
            }
        }
    }

    fun exportBackup(onResult: (jsonString: String, shareIntent: Intent) -> Unit) {
        viewModelScope.launch {
            try {
                val sessions = repository.getAllSessionsDirect()
                val json = CashBackupManager.exportToJson(sessions)
                val intent = CashBackupManager.createShareIntent(getApplication(), json)
                onResult(json, intent)
                _uiEvent.emit(UiEvent.ShowToast("Sauvegarde exportée (${sessions.size} session(s)) avec checksum SHA-256"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur d'export : ${e.localizedMessage}"))
            }
        }
    }

    fun exportBackupToUri(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val sessions = repository.getAllSessionsDirect()
                val json = CashBackupManager.exportToJson(sessions)
                val ok = CashBackupManager.writeToUri(getApplication(), uri, json)
                if (ok) {
                    _uiEvent.emit(UiEvent.ShowToast("Sauvegarde enregistrée avec succès"))
                } else {
                    _uiEvent.emit(UiEvent.ShowToast("Échec de l'écriture du fichier"))
                }
                onComplete(ok)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowToast("Erreur : ${e.localizedMessage}"))
                onComplete(false)
            }
        }
    }

    fun readBackupFromUri(uri: Uri): String? {
        return CashBackupManager.readFromUri(getApplication(), uri)
    }

    fun parseBackup(jsonString: String): BackupData? {
        return try {
            CashBackupManager.parseBackup(jsonString)
        } catch (e: Exception) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowToast("Fichier non valide : ${e.localizedMessage}"))
            }
            null
        }
    }

    fun restoreBackup(
        backupData: BackupData,
        mode: RestoreMode,
        onComplete: (RestoreResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = CashBackupManager.executeRestore(
                context = getApplication(),
                db = db,
                backupData = backupData,
                mode = mode
            )
            if (result.success) {
                _selectedSessionId.value = null
                _uiEvent.emit(UiEvent.ShowToast(result.message))
            } else {
                _uiEvent.emit(UiEvent.ShowToast(result.message))
            }
            onComplete(result)
        }
    }
}
