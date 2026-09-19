package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.CashTab
import com.example.ui.components.GlassBottomNavBar
import com.example.ui.components.WaterDropletBackground
import com.example.ui.dialogs.AddDisbursementDialog
import com.example.ui.dialogs.AddReplenishmentDialog
import com.example.ui.dialogs.CloseCashDialog
import com.example.ui.dialogs.NewSessionDialog
import com.example.ui.screens.ClotureScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DecaissementScreen
import com.example.ui.screens.EncaissementScreen
import com.example.ui.screens.HistoriqueScreen
import com.example.ui.screens.ParametresScreen
import com.example.ui.theme.GlassBorderTop
import com.example.ui.theme.GlassPureWhite
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWaterBlue
import com.example.ui.theme.GlassWaterBlueBg
import com.example.ui.theme.SymphonixBlue
import com.example.ui.theme.SymphonixDeepBlue
import com.example.ui.theme.SymphonixLightBlue
import com.example.util.CashPdfGenerator
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashScreen(
    viewModel: CashViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val selectedSessionId by viewModel.selectedSessionId.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(CashTab.DASHBOARD) }
    var displayedTab by remember { mutableStateOf(CashTab.DASHBOARD) }
    var isTabLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab != displayedTab) {
            isTabLoading = true
            kotlinx.coroutines.delay(160)
            displayedTab = selectedTab
            isTabLoading = false
        }
    }

    var showAddReplenishmentDialog by remember { mutableStateOf(false) }
    var showAddDisbursementDialog by remember { mutableStateOf(false) }
    var showCloseCashDialog by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.SharePdf -> {
                    CashPdfGenerator.sharePdf(context, event.file, event.title)
                }
            }
        }
    }

    WaterDropletBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Top Water Droplet Navigation Header with Symphonix Fox Branding
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val headerShape = RoundedCornerShape(22.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = headerShape,
                                ambientColor = SymphonixBlue.copy(alpha = 0.15f),
                                spotColor = SymphonixDeepBlue.copy(alpha = 0.2f)
                            )
                            .clip(headerShape)
                            .background(Color(0xE6FFFFFF))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        GlassBorderTop,
                                        Color(0x80FFFFFF),
                                        SymphonixBlue.copy(alpha = 0.25f)
                                    )
                                ),
                                shape = headerShape
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Fox Mascot Emblem
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, SymphonixBlue.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_symphonix_fox),
                                    contentDescription = "Symphonix Mascot",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "SYMPHONIX",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            fontSize = 15.sp
                                        ),
                                        color = SymphonixDeepBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${selectedTab.title}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = SymphonixBlue
                                    )
                                }
                                currentSession?.let { details ->
                                    Text(
                                        text = "${details.session.reference} • ${details.session.dateText}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                        color = GlassTextMuted
                                    )
                                }
                            }
                        }

                        // Quick PDF Action Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SymphonixLightBlue)
                                .border(1.dp, SymphonixBlue.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { viewModel.exportAndSharePdf() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = "Générer PDF",
                                    tint = SymphonixBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                GlassBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                ) {
                    AnimatedContent(
                        targetState = displayedTab,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (slideInHorizontally(
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                                initialOffsetX = { fullWidth -> direction * (fullWidth / 4) }
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 240)
                            )) togetherWith (slideOutHorizontally(
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                                targetOffsetX = { fullWidth -> -direction * (fullWidth / 4) }
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 180)
                            ))
                        },
                        label = "tabTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            CashTab.DASHBOARD -> {
                                DashboardScreen(
                                    sessionDetails = currentSession,
                                    onNavigateTab = { selectedTab = it },
                                    onOpenReplenishDialog = { showAddReplenishmentDialog = true },
                                    onOpenDisburseDialog = { showAddDisbursementDialog = true },
                                    onOpenCloseDialog = { showCloseCashDialog = true },
                                    onExportPdf = { viewModel.exportAndSharePdf() }
                                )
                            }
                            CashTab.ENCAISSEMENT -> {
                                EncaissementScreen(
                                    sessionDetails = currentSession,
                                    onOpenAddDialog = { showAddReplenishmentDialog = true },
                                    onDeleteReplenishment = { id -> viewModel.deleteReplenishment(id) }
                                )
                            }
                            CashTab.DECAISSEMENT -> {
                                DecaissementScreen(
                                    sessionDetails = currentSession,
                                    onOpenAddDialog = { showAddDisbursementDialog = true },
                                    onDeleteDisbursement = { id -> viewModel.deleteDisbursement(id) }
                                )
                            }
                            CashTab.HISTORIQUE -> {
                                HistoriqueScreen(
                                    allSessions = allSessions,
                                    selectedSessionId = selectedSessionId ?: currentSession?.session?.id,
                                    onSelectSession = { id ->
                                        viewModel.selectSession(id)
                                        selectedTab = CashTab.DASHBOARD
                                    },
                                    onExportPdf = { sessionDetails ->
                                        viewModel.exportAndSharePdfForSession(sessionDetails)
                                    },
                                    onDeleteSession = { id ->
                                        viewModel.deleteSession(id)
                                    }
                                )
                            }
                            CashTab.CLOTURE -> {
                                ClotureScreen(
                                    sessionDetails = currentSession,
                                    onOpenCloseDialog = { showCloseCashDialog = true },
                                    onReopenSession = { viewModel.reopenSession() },
                                    onOpenNewSessionDialog = { showNewSessionDialog = true }
                                )
                            }
                            CashTab.PARAMETRES -> {
                                ParametresScreen(
                                    sessionDetails = currentSession,
                                    onSaveSettings = { establishmentName, establishmentSubTitle, responsibleName, managerName, currency, initialFund ->
                                        viewModel.updateSettings(
                                            establishmentName = establishmentName,
                                            establishmentSubTitle = establishmentSubTitle,
                                            responsibleName = responsibleName,
                                            managerName = managerName,
                                            currency = currency,
                                            initialFund = initialFund
                                        )
                                    },
                                    onExportPdf = { viewModel.exportAndSharePdf() },
                                    onExportBackup = { callback ->
                                        viewModel.exportBackup { json, intent ->
                                            callback(json, intent)
                                        }
                                    },
                                    onParseBackup = { json ->
                                        viewModel.parseBackup(json)
                                    },
                                    onRestoreBackup = { data, mode, callback ->
                                        viewModel.restoreBackup(data, mode) { result ->
                                            callback(result)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Fluid loading transition indicator between pages
                    if (isTabLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x33F8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xF5FFFFFF))
                                    .border(1.dp, SymphonixBlue.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 24.dp, vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = SymphonixBlue,
                                        strokeWidth = 3.dp
                                    )
                                    Text(
                                        text = "Chargement de ${selectedTab.title}...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SymphonixDeepBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Input Dialogs
    if (showAddReplenishmentDialog) {
        currentSession?.let { details ->
            AddReplenishmentDialog(
                currency = details.session.currency,
                onDismiss = { showAddReplenishmentDialog = false },
                onConfirm = { amount, reason, source, time ->
                    viewModel.addReplenishment(amount, reason, source, time)
                    showAddReplenishmentDialog = false
                }
            )
        }
    }

    if (showAddDisbursementDialog) {
        currentSession?.let { details ->
            AddDisbursementDialog(
                currency = details.session.currency,
                onDismiss = { showAddDisbursementDialog = false },
                onConfirm = { amount, designation, parentCat, subCat, recipient, time ->
                    viewModel.addDisbursement(amount, designation, parentCat, subCat, recipient, time)
                    showAddDisbursementDialog = false
                }
            )
        }
    }

    if (showCloseCashDialog) {
        currentSession?.let { details ->
            CloseCashDialog(
                theoreticalBalance = details.theoreticalBalance,
                currency = details.session.currency,
                onDismiss = { showCloseCashDialog = false },
                onConfirm = { countedCash, closingTime ->
                    viewModel.closeSession(countedCash, closingTime)
                    showCloseCashDialog = false
                }
            )
        }
    }

    if (showNewSessionDialog) {
        val lastBalance = currentSession?.theoreticalBalance ?: 150000.0
        val curr = currentSession?.session?.currency ?: "DA"
        NewSessionDialog(
            lastTheoreticalBalance = lastBalance,
            currency = curr,
            onDismiss = { showNewSessionDialog = false },
            onConfirm = { fund, source, estName, respName ->
                viewModel.createNewSession(
                    initialFund = fund,
                    initialFundSource = source,
                    establishmentName = estName,
                    responsibleName = respName
                )
                showNewSessionDialog = false
                selectedTab = CashTab.DASHBOARD
            }
        )
    }
}
