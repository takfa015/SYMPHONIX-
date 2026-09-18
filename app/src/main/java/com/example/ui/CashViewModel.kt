package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.SessionWithDetails
import com.example.data.repository.CashRepository
import com.example.util.CashPdfGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val repository: CashRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CashRepository(db.cashDao())
        viewModelScope.launch {
            repository.ensureInitialData()
        }
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

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    fun selectSession(sessionId: Long) {
        _selectedSessionId.value = sessionId
    }

    fun addReplenishment(
        amount: Double,
        reason: String,
        sourceLocation: String,
        time: String = ""
    ) {
        val session = currentSession.value?.session ?: return
        val effectiveTime = if (time.isNotBlank()) time else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            repository.addReplenishment(
                sessionId = session.id,
                amount = amount,
                time = effectiveTime,
                reason = reason,
                sourceLocation = sourceLocation
            )
            _uiEvent.emit(UiEvent.ShowToast("Alimentation de ${CashPdfGenerator.formatAmount(amount, session.currency)} enregistrée"))
        }
    }

    fun deleteReplenishment(id: Long) {
        viewModelScope.launch {
            repository.deleteReplenishment(id)
            _uiEvent.emit(UiEvent.ShowToast("Alimentation supprimée"))
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
        val session = currentSession.value?.session ?: return
        val effectiveTime = if (time.isNotBlank()) time else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            repository.addDisbursement(
                sessionId = session.id,
                amount = amount,
                time = effectiveTime,
                designation = designation,
                parentCategory = parentCategory,
                subCategory = subCategory,
                recipient = recipient
            )
            _uiEvent.emit(UiEvent.ShowToast("Décaissement de ${CashPdfGenerator.formatAmount(amount, session.currency)} enregistré"))
        }
    }

    fun deleteDisbursement(id: Long) {
        viewModelScope.launch {
            repository.deleteDisbursement(id)
            _uiEvent.emit(UiEvent.ShowToast("Décaissement supprimé"))
        }
    }

    fun closeSession(countedCash: Double, closingTime: String = "") {
        val session = currentSession.value?.session ?: return
        val effectiveTime = if (closingTime.isNotBlank()) closingTime else SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            repository.closeSession(
                sessionId = session.id,
                countedCash = countedCash,
                closingTime = effectiveTime
            )
            _uiEvent.emit(UiEvent.ShowToast("Caisse clôturée avec succès à $effectiveTime"))
        }
    }

    fun reopenSession() {
        val session = currentSession.value?.session ?: return
        viewModelScope.launch {
            repository.reopenSession(session.id)
            _uiEvent.emit(UiEvent.ShowToast("Session rouverte pour modifications"))
        }
    }

    fun createNewSession(
        initialFund: Double,
        initialFundSource: String = "Dotation de caisse",
        establishmentName: String = "Établissement commercial",
        responsibleName: String = "Responsable de caisse"
    ) {
        val time = SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            val newId = repository.createNewSession(
                initialFund = initialFund,
                initialFundTime = time,
                initialFundSource = initialFundSource,
                establishmentName = establishmentName,
                responsibleName = responsibleName
            )
            _selectedSessionId.value = newId
            _uiEvent.emit(UiEvent.ShowToast("Nouvelle session de caisse ouverte !"))
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
                _uiEvent.emit(UiEvent.ShowToast("Erreur de génération PDF: ${e.localizedMessage}"))
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
        val session = currentSession.value?.session ?: return
        viewModelScope.launch {
            repository.updateSessionSettings(
                sessionId = session.id,
                establishmentName = establishmentName.trim(),
                establishmentSubTitle = establishmentSubTitle.trim(),
                responsibleName = responsibleName.trim(),
                managerName = managerName.trim(),
                currency = currency.trim(),
                initialFund = initialFund
            )
            _uiEvent.emit(UiEvent.ShowToast("Paramètres enregistrés avec succès"))
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_selectedSessionId.value == sessionId) {
                _selectedSessionId.value = null
            }
            _uiEvent.emit(UiEvent.ShowToast("Session supprimée"))
        }
    }
}
