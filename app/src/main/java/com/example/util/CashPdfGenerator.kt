package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.SessionWithDetails
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * High-precision A4 PDF generator conforming to the official SYMPHONIX Brand Guide
 * and the daily cash recap voucher specification.
 *
 * Enhanced with:
 * - Proper letter spacing & sans-serif font weights avoiding cramped text
 * - Clean text measurement and dynamic column balancing
 * - Sharp brand visuals & watermark
 */
object CashPdfGenerator {

    private val decimalFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        DecimalFormat("#,##0.00", symbols)
    }

    private val percentFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
            decimalSeparator = ','
        }
        DecimalFormat("0.00", symbols)
    }

    fun formatAmount(amountCents: Long, currency: String = "DA"): String {
        return Money.format(amountCents, currency)
    }

    fun formatAmount(amount: Double, currency: String = "DA"): String {
        return Money.format(Money.doubleToCents(amount), currency)
    }

    fun formatPercent(percent: Double): String {
        return "${percentFormat.format(percent)} %"
    }

    /**
     * Generates an official SYMPHONIX A4 PDF voucher in cache directory and returns the File.
     */
    fun generatePdfFile(context: Context, details: SessionWithDetails): File {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions in PostScript points: 595 x 842 pt
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawDocument(context, canvas, details, pageWidth, pageHeight)

        pdfDocument.finishPage(page)

        val outputDir = File(context.cacheDir, "pdf_reports").apply { mkdirs() }
        val filename = "SYMPHONIX_Recap_Caisse_${details.session.reference}.pdf"
        val outputFile = File(outputDir, filename)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun sharePdf(context: Context, file: File, title: String = "Partager la Fiche de Caisse") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun createPaint(
        color: Int = Color.BLACK,
        textSize: Float = 8f,
        isBold: Boolean = false,
        isItalic: Boolean = false,
        letterSpacing: Float = 0f
    ): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            this.color = color
            this.textSize = textSize
            val style = when {
                isBold && isItalic -> Typeface.BOLD_ITALIC
                isBold -> Typeface.BOLD
                isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            this.typeface = Typeface.create(Typeface.SANS_SERIF, style)
            // DO NOT set letterSpacing on Android PDF Canvas:
            // Android's Skia PDF backend inserts artificial word-break tags/kerning spaces
            // causing words to split in viewers like Samsung Notes (e.g. "compt age", "parfai te").
        }
    }

    private fun drawDocument(
        context: Context,
        canvas: Canvas,
        details: SessionWithDetails,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val session = details.session
        val leftMargin = 32f
        val rightMargin = pageWidth - 32f
        val contentWidth = rightMargin - leftMargin

        // 0. Background canvas (Crisp Pure White)
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        var y = 42f

        // 1. Header Title & Company Logo / Brand Tag
        val companyLogo = CompanyLogoManager.getLogoBitmap(context)
        val textLeftStart: Float
        if (companyLogo != null) {
            val maxLogoW = 52f
            val maxLogoH = 34f
            val ratio = companyLogo.width.toFloat() / companyLogo.height.toFloat().coerceAtLeast(1f)
            val drawW = if (ratio > 1f) maxLogoW else (maxLogoH * ratio).coerceAtMost(maxLogoW)
            val drawH = if (ratio > 1f) (maxLogoW / ratio).coerceAtMost(maxLogoH) else maxLogoH
            val logoDestRect = RectF(leftMargin, y - 14f, leftMargin + drawW, y - 14f + drawH)
            val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(companyLogo, null, logoDestRect, logoPaint)
            textLeftStart = leftMargin + drawW + 10f
        } else {
            textLeftStart = leftMargin
        }

        val brandPaint = createPaint(
            color = Color.rgb(20, 107, 255), // Symphonix Blue
            textSize = 8.5f,
            isBold = true,
            letterSpacing = 0.06f
        )
        val brandTag = "SYMPHONIX  |  " + (if (session.establishmentName.isNotBlank()) session.establishmentName else "CAISSE").uppercase()
        canvas.drawText(brandTag, textLeftStart, y - 11f, brandPaint)

        val titlePaint = createPaint(
            color = Color.rgb(11, 46, 115), // Symphonix Deep Blue
            textSize = 13.5f,
            isBold = true,
            letterSpacing = 0.03f
        )
        canvas.drawText("FICHE DE RÉCAPITULATIF DE CAISSE", textLeftStart, y + 4f, titlePaint)

        // Date pill badge on top right
        val dateText = "Date : ${session.dateText}"
        val dateMeasurePaint = createPaint(
            color = Color.rgb(22, 101, 52),
            textSize = 8.5f,
            isBold = true,
            letterSpacing = 0.02f
        )
        val dateWidth = dateMeasurePaint.measureText(dateText)
        val datePillRect = RectF(rightMargin - dateWidth - 16f, y - 13f, rightMargin, y + 3f)
        val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 252, 231) // light mint green
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(datePillRect, 4f, 4f, pillBgPaint)
        canvas.drawText(dateText, rightMargin - dateWidth - 8f, y - 1f, dateMeasurePaint)

        y += 14f
        // Subtitle
        val subPaint = createPaint(
            color = Color.rgb(71, 85, 105),
            textSize = 8.2f,
            isBold = false,
            letterSpacing = 0.015f
        )
        canvas.drawText("Journal chronologique des décaissements et rapprochement du fond de roulement", textLeftStart, y, subPaint)

        // Document Reference
        val refText = "Réf. Document : ${session.reference}"
        val refWidth = subPaint.measureText(refText)
        canvas.drawText(refText, rightMargin - refWidth, y, subPaint)

        y += 16f

        // 2. The 4 KPI Cards
        val cardWidth = (contentWidth - 24f) / 4f
        val cardHeight = 44f
        val cardY = y

        // Card 1: FOND INITIAL / DISPONIBLE
        drawKpiCard(
            canvas = canvas,
            x = leftMargin,
            y = cardY,
            width = cardWidth,
            height = cardHeight,
            title = if (details.replenishments.isNotEmpty()) "FOND INITIAL + APPORTS" else "FOND INITIAL",
            titleColor = Color.rgb(71, 85, 105),
            amount = formatAmount(details.totalAvailableFund, session.currency),
            amountColor = Color.rgb(15, 23, 42),
            sub = "Dotation de départ : ${formatAmount(session.initialFund, session.currency)}",
            bgColor = Color.rgb(248, 250, 252),
            borderColor = Color.rgb(226, 232, 240)
        )

        // Card 2: TOTAL DÉPENSES SAISIES
        val percentSpent = details.expensePercentageOfFund
        drawKpiCard(
            canvas = canvas,
            x = leftMargin + cardWidth + 8f,
            y = cardY,
            width = cardWidth,
            height = cardHeight,
            title = "TOTAL DÉPENSES SAISIES",
            titleColor = Color.rgb(180, 83, 9),
            amount = formatAmount(details.totalDisbursements, session.currency),
            amountColor = Color.rgb(180, 83, 9),
            sub = "${details.disbursements.size} opérations (${formatPercent(percentSpent)})",
            bgColor = Color.rgb(255, 251, 235),
            borderColor = Color.rgb(254, 215, 170)
        )

        // Card 3: RESTE PHYSIQUE CONSTATÉ
        val countedAmount = if (session.isClosed && session.countedCash != null) {
            formatAmount(session.countedCash, session.currency)
        } else {
            "En attente"
        }
        val countedSub = if (session.isClosed) "Espèces en caisse (${session.closingTime ?: ""})" else "Caisse active (non clôturée)"
        drawKpiCard(
            canvas = canvas,
            x = leftMargin + (cardWidth + 8f) * 2f,
            y = cardY,
            width = cardWidth,
            height = cardHeight,
            title = "RESTE PHYSIQUE CONSTATÉ",
            titleColor = Color.rgb(22, 101, 52),
            amount = countedAmount,
            amountColor = Color.rgb(22, 101, 52),
            sub = countedSub,
            bgColor = Color.rgb(240, 253, 244),
            borderColor = Color.rgb(187, 247, 208)
        )

        // Card 4: ÉCART DE RAPPROCHEMENT
        val (ecartAmountStr, ecartSubStr, ecartColor, ecartBg, ecartBorder) = if (!session.isClosed) {
            Tuple5(
                "---",
                "Clôture requise",
                Color.rgb(100, 116, 139),
                Color.rgb(248, 250, 252),
                Color.rgb(226, 232, 240)
            )
        } else if (details.isBalanced) {
            Tuple5(
                "0 ${session.currency}",
                "Concordance exacte (100 %)",
                Color.rgb(22, 101, 52),
                Color.rgb(240, 253, 244),
                Color.rgb(187, 247, 208)
            )
        } else {
            val prefix = if (details.discrepancy > 0) "+" else ""
            Tuple5(
                "$prefix${formatAmount(details.discrepancy, session.currency)}",
                if (details.discrepancy > 0) "Excédent constaté" else "Déficit constaté",
                Color.rgb(185, 28, 28),
                Color.rgb(254, 242, 242),
                Color.rgb(254, 202, 202)
            )
        }

        drawKpiCard(
            canvas = canvas,
            x = leftMargin + (cardWidth + 8f) * 3f,
            y = cardY,
            width = cardWidth,
            height = cardHeight,
            title = "ÉCART DE RAPPROCHEMENT",
            titleColor = ecartColor,
            amount = ecartAmountStr,
            amountColor = ecartColor,
            sub = ecartSubStr,
            bgColor = ecartBg,
            borderColor = ecartBorder
        )

        y += cardHeight + 14f

        // 3. Optional In-Day Replenishments Table (if any alimentations exist)
        if (details.replenishments.isNotEmpty()) {
            y = drawReplenishmentsTable(canvas, details, leftMargin, y, contentWidth, rightMargin)
            y += 12f
        }

        // 4. Main Disbursements Table
        y = drawDisbursementsTable(canvas, details, leftMargin, y, contentWidth, rightMargin)
        y += 14f

        // 5. Lower Section: Left Column (Ventilation + Observation) & Right Column (Rapprochement Table)
        val colWidth = (contentWidth - 14f) / 2f
        val leftColX = leftMargin
        val rightColX = leftMargin + colWidth + 14f

        drawLowerSection(
            canvas = canvas,
            details = details,
            leftX = leftColX,
            rightX = rightColX,
            width = colWidth,
            startY = y
        )

        // 6. Signatures Area
        val sigY = pageHeight - 110f
        drawSignaturesArea(canvas, details, leftMargin, sigY, colWidth, rightMargin)

        // 7. Footer line
        drawFooter(canvas, details, leftMargin, pageHeight - 25f, rightMargin)
    }

    private fun drawKpiCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        titleColor: Int,
        amount: String,
        amountColor: Int,
        sub: String,
        bgColor: Int,
        borderColor: Int
    ) {
        val rect = RectF(x, y, x + width, y + height)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        // Title
        val tPaint = createPaint(titleColor, 6.8f, isBold = true, letterSpacing = 0.03f)
        canvas.drawText(title, x + 7f, y + 11f, tPaint)

        // Amount
        val aPaint = createPaint(amountColor, 10.2f, isBold = true, letterSpacing = 0.02f)
        canvas.drawText(amount, x + 7f, y + 25f, aPaint)

        // Subtitle
        val sPaint = createPaint(Color.rgb(100, 116, 139), 6.2f, isBold = false, letterSpacing = 0.01f)
        canvas.drawText(sub, x + 7f, y + 37f, sPaint)
    }

    private fun drawReplenishmentsTable(
        canvas: Canvas,
        details: SessionWithDetails,
        leftMargin: Float,
        startY: Float,
        contentWidth: Float,
        rightMargin: Float
    ): Float {
        var y = startY
        val session = details.session

        // Title bar
        val titleBarHeight = 16f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 107, 255) // Symphonix Blue
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + titleBarHeight, bgPaint)

        val tPaint = createPaint(Color.WHITE, 7.5f, isBold = true, letterSpacing = 0.03f)
        canvas.drawText("Journal des Alimentations & Apports de Caisse en cours de journée", leftMargin + 8f, y + 11f, tPaint)

        val countText = "${details.replenishments.size} apport(s) constaté(s)"
        val countW = tPaint.measureText(countText)
        canvas.drawText(countText, rightMargin - countW - 8f, y + 11f, tPaint)

        y += titleBarHeight

        // Table Header
        val colN = leftMargin + 6f
        val colHeure = leftMargin + 25f
        val colMotif = leftMargin + 65f
        val colSource = leftMargin + 220f
        val colMontant = rightMargin - 8f

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(239, 246, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + 14f, headerPaint)

        val colHeadPaint = createPaint(Color.rgb(30, 64, 175), 6.8f, isBold = true, letterSpacing = 0.02f)
        canvas.drawText("N°", colN, y + 10f, colHeadPaint)
        canvas.drawText("Heure", colHeure, y + 10f, colHeadPaint)
        canvas.drawText("Motif / Justification", colMotif, y + 10f, colHeadPaint)
        canvas.drawText("Provenance / Emplacement source", colSource, y + 10f, colHeadPaint)
        val mLabel = "Montant (${session.currency})"
        canvas.drawText(mLabel, colMontant - colHeadPaint.measureText(mLabel), y + 10f, colHeadPaint)

        y += 14f

        // Rows
        val rowHeight = 15f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        val rowTextPaint = createPaint(Color.rgb(30, 41, 59), 7f, isBold = false, letterSpacing = 0.015f)
        val rowAmtPaint = createPaint(Color.rgb(30, 41, 59), 7f, isBold = true, letterSpacing = 0.02f)

        details.replenishments.forEachIndexed { index, item ->
            val isCancelled = item.isCancelled
            if (index % 2 == 1) {
                val zebraPaint = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(leftMargin, y, rightMargin, y + rowHeight, zebraPaint)
            }

            val rTextPaint = createPaint(
                if (isCancelled) Color.rgb(148, 163, 184) else Color.rgb(30, 41, 59),
                7f,
                isBold = false,
                letterSpacing = 0.015f
            ).apply {
                if (isCancelled) flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            val rAmtPaint = createPaint(
                if (isCancelled) Color.rgb(148, 163, 184) else Color.rgb(30, 41, 59),
                7f,
                isBold = !isCancelled,
                letterSpacing = 0.02f
            ).apply {
                if (isCancelled) flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            canvas.drawText(String.format("%02d", item.orderNumber), colN, y + 10.5f, rTextPaint)
            canvas.drawText(item.time, colHeure, y + 10.5f, rTextPaint)
            val reasonText = if (isCancelled) "[ANNULÉ] ${item.reason}" else item.reason
            canvas.drawText(reasonText, colMotif, y + 10.5f, rTextPaint)
            canvas.drawText(item.sourceLocation, colSource, y + 10.5f, rTextPaint)

            val amtText = formatAmount(item.amount, session.currency)
            canvas.drawText(amtText, colMontant - rAmtPaint.measureText(amtText), y + 10.5f, rAmtPaint)

            canvas.drawLine(leftMargin, y + rowHeight, rightMargin, y + rowHeight, borderPaint)
            y += rowHeight
        }

        // Replenishment Total bar
        val totalBarHeight = 14f
        val totalBg = Paint().apply {
            color = Color.rgb(219, 234, 254)
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + totalBarHeight, totalBg)

        val totalPaint = createPaint(Color.rgb(30, 58, 138), 7.2f, isBold = true, letterSpacing = 0.03f)
        val replTotLabel = if (details.cancelledReplenishments.isNotEmpty()) {
            "TOTAL DES ALIMENTATIONS ACTIVES (${details.activeReplenishments.size} entrées, ${details.cancelledReplenishments.size} annulée(s)) :"
        } else {
            "TOTAL DES ALIMENTATIONS ENTRÉES :"
        }
        canvas.drawText(replTotLabel, colMotif, y + 10f, totalPaint)

        val totalAmt = formatAmount(details.totalReplenishments, session.currency)
        canvas.drawText(totalAmt, colMontant - totalPaint.measureText(totalAmt), y + 10f, totalPaint)

        y += totalBarHeight
        return y
    }

    private fun drawDisbursementsTable(
        canvas: Canvas,
        details: SessionWithDetails,
        leftMargin: Float,
        startY: Float,
        contentWidth: Float,
        rightMargin: Float
    ): Float {
        var y = startY
        val session = details.session

        // Deep Teal/Blue header
        val headerHeight = 21f
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(11, 46, 115) // Symphonix Deep Blue
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + headerHeight, headerBg)

        val headPaint = createPaint(Color.WHITE, 8.5f, isBold = true, letterSpacing = 0.03f)
        canvas.drawText("Journal Nominatif des Décaissements Effectués", leftMargin + 8f, y + 14f, headPaint)

        val subHeadRightPaint = createPaint(Color.WHITE, 7f, isBold = false, letterSpacing = 0.02f)
        val subRight = "Devise : Dinar Algérien (${session.currency}) — Pointage chronologique"
        val subRightW = subHeadRightPaint.measureText(subRight)
        canvas.drawText(subRight, rightMargin - subRightW - 8f, y + 14f, subHeadRightPaint)

        y += headerHeight

        // Sub-header columns
        val subHeaderHeight = 15f
        val subHeaderBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 107, 255) // Symphonix Blue
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + subHeaderHeight, subHeaderBg)

        val colN = leftMargin + 6f
        val colHeure = leftMargin + 25f
        val colDesig = leftMargin + 65f
        val colCat = leftMargin + 240f
        val colMontant = rightMargin - 52f
        val colPart = rightMargin - 8f

        val colLabelPaint = createPaint(Color.WHITE, 7f, isBold = true, letterSpacing = 0.02f)

        canvas.drawText("N°", colN, y + 10.5f, colLabelPaint)
        canvas.drawText("Heure", colHeure, y + 10.5f, colLabelPaint)
        canvas.drawText("Désignation / Bénéficiaire", colDesig, y + 10.5f, colLabelPaint)
        canvas.drawText("Catégorie", colCat, y + 10.5f, colLabelPaint)

        val mLabel = "Montant (${session.currency})"
        canvas.drawText(mLabel, colMontant - colLabelPaint.measureText(mLabel), y + 10.5f, colLabelPaint)

        val pLabel = "Part (%)"
        canvas.drawText(pLabel, colPart - colLabelPaint.measureText(pLabel), y + 10.5f, colLabelPaint)

        y += subHeaderHeight

        // Rows
        val rowHeight = 17f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        val totalDecaisse = details.totalDisbursements
        val textPaint = createPaint(Color.rgb(30, 41, 59), 7.5f, isBold = false, letterSpacing = 0.015f)
        val amtPaint = createPaint(Color.rgb(15, 23, 42), 7.5f, isBold = true, letterSpacing = 0.02f)
        val partPaint = createPaint(Color.rgb(71, 85, 105), 7.2f, isBold = false, letterSpacing = 0.01f)

        details.disbursements.forEachIndexed { index, item ->
            val isCancelled = item.isCancelled
            // Subtle alternating tint
            if (index % 2 == 1) {
                val zebraPaint = Paint().apply {
                    color = Color.rgb(250, 252, 251)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(leftMargin, y, rightMargin, y + rowHeight, zebraPaint)
            }

            val dTextPaint = createPaint(
                if (isCancelled) Color.rgb(148, 163, 184) else Color.rgb(30, 41, 59),
                7.5f,
                isBold = false,
                letterSpacing = 0.015f
            ).apply {
                if (isCancelled) flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            val dAmtPaint = createPaint(
                if (isCancelled) Color.rgb(148, 163, 184) else Color.rgb(15, 23, 42),
                7.5f,
                isBold = !isCancelled,
                letterSpacing = 0.02f
            ).apply {
                if (isCancelled) flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            val dPartPaint = createPaint(
                if (isCancelled) Color.rgb(148, 163, 184) else Color.rgb(71, 85, 105),
                7.2f,
                isBold = false,
                letterSpacing = 0.01f
            )

            // N°
            canvas.drawText(String.format("%02d", item.orderNumber), colN, y + 11.5f, dTextPaint)
            // Heure
            canvas.drawText(item.time, colHeure, y + 11.5f, dTextPaint)

            // Désignation (Ellipsize if necessary to prevent overlap with Catégorie)
            val maxDesigWidth = (colCat - 6f) - colDesig
            val rawDesig = if (isCancelled) "[ANNULÉ] ${item.designation}" else item.designation
            val safeDesig = ellipsizeText(rawDesig, dTextPaint, maxDesigWidth)
            canvas.drawText(safeDesig, colDesig, y + 11.5f, dTextPaint)

            // Catégorie Badge
            val catText = if (isCancelled) "ANNULÉ" else item.fullCategory
            val catBadgePaint = createPaint(
                if (isCancelled) Color.rgb(153, 27, 27) else if (item.parentCategory.contains("Personnel", true)) Color.rgb(30, 64, 175) else Color.rgb(6, 95, 70),
                6.8f,
                isBold = true,
                letterSpacing = 0.02f
            )
            val catBadgeW = catBadgePaint.measureText(catText) + 8f
            val catPill = RectF(colCat - 3f, y + 3f, colCat + catBadgeW, y + rowHeight - 3f)
            val catPillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isCancelled) {
                    Color.rgb(254, 226, 226) // soft red
                } else if (item.parentCategory.contains("Personnel", true)) {
                    Color.rgb(219, 234, 254) // soft blue
                } else {
                    Color.rgb(209, 250, 229) // soft emerald
                }
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(catPill, 3f, 3f, catPillPaint)
            canvas.drawText(catText, colCat + 1f, y + 11.5f, catBadgePaint)

            // Montant
            val amtStr = formatAmount(item.amount, session.currency)
            canvas.drawText(amtStr, colMontant - dAmtPaint.measureText(amtStr), y + 11.5f, dAmtPaint)

            // Part %
            val partStr = if (isCancelled) "—" else {
                val part = if (totalDecaisse > 0) (item.amount.toDouble() / totalDecaisse.toDouble()) * 100.0 else 0.0
                formatPercent(part)
            }
            canvas.drawText(partStr, colPart - dPartPaint.measureText(partStr), y + 11.5f, dPartPaint)

            // Line separator
            canvas.drawLine(leftMargin, y + rowHeight, rightMargin, y + rowHeight, borderPaint)
            y += rowHeight
        }

        // Total Row
        val totalRowHeight = 18f
        val totalRowBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(240, 253, 250)
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + totalRowHeight, totalRowBg)

        val totLabelPaint = createPaint(Color.rgb(15, 23, 42), 7.8f, isBold = true, letterSpacing = 0.03f)
        val totalLabel = if (details.cancelledDisbursements.isNotEmpty()) {
            "TOTAL DÉCAISSEMENTS ACTIFS (${details.activeDisbursements.size} OPÉRATIONS, ${details.cancelledDisbursements.size} ANNULÉE(S)) :"
        } else {
            "TOTAL DES DÉCAISSEMENTS ENREGISTRÉS (${details.disbursements.size} OPÉRATIONS) :"
        }
        canvas.drawText(totalLabel, colCat - 40f, y + 12f, totLabelPaint)

        val totAmtPaint = createPaint(Color.rgb(180, 83, 9), 7.8f, isBold = true, letterSpacing = 0.02f)
        val totalAmtStr = formatAmount(totalDecaisse, session.currency)
        canvas.drawText(totalAmtStr, colMontant - totAmtPaint.measureText(totalAmtStr), y + 12f, totAmtPaint)

        val totPartPaint = createPaint(Color.rgb(15, 23, 42), 7.5f, isBold = true, letterSpacing = 0.02f)
        val hundredPercent = "100,00 %"
        canvas.drawText(hundredPercent, colPart - totPartPaint.measureText(hundredPercent), y + 12f, totPartPaint)

        val doubleBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 107, 255)
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(leftMargin, y + totalRowHeight, rightMargin, y + totalRowHeight, doubleBorder)

        y += totalRowHeight
        return y
    }

    private fun ellipsizeText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "..."
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return if (end > 0) text.substring(0, end) + ellipsis else ""
    }

    private fun drawLowerSection(
        canvas: Canvas,
        details: SessionWithDetails,
        leftX: Float,
        rightX: Float,
        width: Float,
        startY: Float
    ) {
        val session = details.session

        // --- LEFT COLUMN: Ventilation by Category ---
        var yLeft = startY
        val secTitlePaint = createPaint(Color.rgb(15, 23, 42), 8.5f, isBold = true, letterSpacing = 0.03f)
        canvas.drawText("Ventilation par Catégorie de Dépense", leftX, yLeft + 8f, secTitlePaint)

        // Accent line
        val accentLinePaint = Paint().apply {
            color = Color.rgb(20, 107, 255) // Symphonix Blue
            strokeWidth = 1.5f
        }
        canvas.drawLine(leftX, yLeft + 12f, leftX + width, yLeft + 12f, accentLinePaint)

        yLeft += 20f

        val catNamePaint = createPaint(Color.rgb(30, 41, 59), 7.5f, isBold = true, letterSpacing = 0.02f)
        val catValPaint = createPaint(Color.rgb(30, 41, 59), 7.5f, isBold = true, letterSpacing = 0.02f)

        details.categoryBreakdowns.forEach { breakdown ->
            canvas.drawText(breakdown.name, leftX, yLeft + 5f, catNamePaint)

            val valText = "${formatAmount(breakdown.amount, session.currency)} (${formatPercent(breakdown.percentage)})"
            val valW = catValPaint.measureText(valText)
            canvas.drawText(valText, leftX + width - valW, yLeft + 5f, catValPaint)

            yLeft += 15f
        }

        // --- RIGHT COLUMN: Pointage & Rapprochement de Caisse Table ---
        var yRight = startY
        canvas.drawText("Pointage & Rapprochement de Caisse", rightX, yRight + 8f, secTitlePaint)
        canvas.drawLine(rightX, yRight + 12f, rightX + width, yRight + 12f, accentLinePaint)
        yRight += 20f

        val rapRowHeight = 14f

        // 1. Fond initial
        drawRapprochementRow(
            canvas, rightX, yRight, width,
            "(+) Fond de roulement initial (${session.initialFundTime})",
            formatAmount(session.initialFund, session.currency),
            isBold = false,
            isNegative = false
        )
        yRight += rapRowHeight

        // 2. Replenishments if any
        if (details.replenishments.isNotEmpty()) {
            drawRapprochementRow(
                canvas, rightX, yRight, width,
                "(+) Apports & alimentations (${details.replenishments.size} entrées)",
                formatAmount(details.totalReplenishments, session.currency),
                isBold = false,
                isNegative = false,
                color = Color.rgb(20, 107, 255)
            )
            yRight += rapRowHeight
        }

        // 3. Fond Total disponible
        drawRapprochementRow(
            canvas, rightX, yRight, width,
            "(=) TOTAL LIQUIDITÉS DISPONIBLES",
            formatAmount(details.totalAvailableFund, session.currency),
            isBold = true,
            isNegative = false,
            color = Color.rgb(15, 23, 42)
        )
        yRight += rapRowHeight

        // 4. Total décaissements
        drawRapprochementRow(
            canvas, rightX, yRight, width,
            "(−) Total dépenses & décaissements",
            formatAmount(details.totalDisbursements, session.currency),
            isBold = false,
            isNegative = true,
            color = Color.rgb(180, 83, 9)
        )
        yRight += rapRowHeight

        // Divider
        val rowDivider = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.6f
        }
        canvas.drawLine(rightX, yRight + 2f, rightX + width, yRight + 2f, rowDivider)
        yRight += 6f

        // 5. Solde théorique
        drawRapprochementRow(
            canvas, rightX, yRight, width,
            "(=) SOLDE THÉORIQUE ATTENDU",
            formatAmount(details.theoreticalBalance, session.currency),
            isBold = true,
            isNegative = false,
            color = Color.rgb(11, 46, 115)
        )
        yRight += rapRowHeight

        // 5b. Comptage physique
        drawRapprochementRow(
            canvas, rightX, yRight, width,
            "(=) RESTE PHYSIQUE RÉEL (ESPÈCES)",
            formatAmount(details.countedCash, session.currency),
            isBold = true,
            isNegative = false,
            color = Color.rgb(22, 101, 52)
        )
        yRight += rapRowHeight

        // Écart de caisse box
        val ecartBoxRect = RectF(rightX, yRight + 2f, rightX + width, yRight + 18f)
        val ecartBgPaint = Paint().apply {
            color = if (details.isBalanced) Color.rgb(240, 253, 244) else Color.rgb(254, 242, 242)
            style = Paint.Style.FILL
        }
        val ecartBorderPaint = Paint().apply {
            color = if (details.isBalanced) Color.rgb(187, 247, 208) else Color.rgb(254, 202, 202)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(ecartBoxRect, 3f, 3f, ecartBgPaint)
        canvas.drawRoundRect(ecartBoxRect, 3f, 3f, ecartBorderPaint)

        // 6. Écart de pointage
        val ecartLabel = if (details.isBalanced) "Écart de pointage (Concordance parfaite) :" else "Écart de pointage constaté :"
        val ecartValStr = if (details.isBalanced) "0 ${session.currency}" else formatAmount(details.discrepancy, session.currency)
        val rapEcartPaint = createPaint(
            if (details.isBalanced) Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28),
            7.5f,
            isBold = true,
            letterSpacing = 0.02f
        )
        canvas.drawText(ecartLabel, rightX + 6f, yRight + 13f, rapEcartPaint)
        val ecartValW = rapEcartPaint.measureText(ecartValStr)
        canvas.drawText(ecartValStr, rightX + width - ecartValW - 6f, yRight + 13f, rapEcartPaint)
    }

    private fun drawRapprochementRow(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        label: String,
        value: String,
        isBold: Boolean,
        isNegative: Boolean,
        color: Int = Color.rgb(30, 41, 59)
    ) {
        val paint = createPaint(color, 7.2f, isBold = isBold, letterSpacing = 0.02f)
        canvas.drawText(label, x + 4f, y + 10f, paint)
        val valW = paint.measureText(value)
        canvas.drawText(value, x + width - valW - 4f, y + 10f, paint)
    }

    private fun drawSignaturesArea(
        canvas: Canvas,
        details: SessionWithDetails,
        leftMargin: Float,
        startY: Float,
        colWidth: Float,
        rightMargin: Float
    ) {
        val height = 48f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }
        val bgPaint = Paint().apply {
            color = Color.rgb(250, 250, 250)
            style = Paint.Style.FILL
        }

        val sigHeaderPaint = createPaint(Color.rgb(71, 85, 105), 6.8f, isBold = true, letterSpacing = 0.03f)
        val sigSubPaint = createPaint(Color.rgb(148, 163, 184), 6.5f, isBold = false, isItalic = true, letterSpacing = 0.02f)

        // Box 1: ÉTABLI PAR (RESPONSABLE DE CAISSE)
        val box1 = RectF(leftMargin, startY, leftMargin + colWidth, startY + height)
        canvas.drawRect(box1, bgPaint)
        canvas.drawRect(box1, borderPaint)

        canvas.drawText("ÉTABLI PAR (${details.session.responsibleName.uppercase()}) :", leftMargin + 8f, startY + 12f, sigHeaderPaint)
        canvas.drawText("Signature & Date (${details.session.dateText})", leftMargin + 8f, startY + 40f, sigSubPaint)

        // Box 2: VÉRIFIÉ & VALIDÉ PAR (DIRECTION / GÉRANCE)
        val rightX = rightMargin - colWidth
        val box2 = RectF(rightX, startY, rightMargin, startY + height)
        canvas.drawRect(box2, bgPaint)
        canvas.drawRect(box2, borderPaint)

        canvas.drawText("VÉRIFIÉ & VALIDÉ PAR (${details.session.managerName.uppercase()}) :", rightX + 8f, startY + 12f, sigHeaderPaint)
        canvas.drawText("Signature & Cachet", rightX + 8f, startY + 40f, sigSubPaint)
        // Integrity footprint badge line
        val hashText = details.session.integrityHash?.take(12)?.uppercase() ?: "NON CLÔTURÉE"
        val integrityText = "Empreinte SHA-256 : $hashText  |  Lignes annulées : ${details.totalCancelledCount}  |  Réouvertures : ${details.session.reopenCount}  |  Document avec empreinte d'intégrité"
        val integPaint = createPaint(Color.rgb(100, 116, 139), 6.5f, isBold = false, letterSpacing = 0.015f)
        val integW = integPaint.measureText(integrityText)
        canvas.drawText(integrityText, (leftMargin + rightMargin) / 2f - integW / 2f, startY + height + 13f, integPaint)
    }

    private fun drawFooter(
        canvas: Canvas,
        details: SessionWithDetails,
        leftMargin: Float,
        y: Float,
        rightMargin: Float
    ) {
        val footerPaint = createPaint(Color.rgb(148, 163, 184), 6.5f, isBold = false, letterSpacing = 0.02f)

        val sep = " — "
        val left = "SYMPHONIX Caisse$sep${details.session.establishmentName}"
        canvas.drawText(left, leftMargin, y, footerPaint)

        val center = "Arrêté du ${details.session.dateText}$sep${if (details.session.isClosed) "Clôture avec empreinte d'intégrité" else "Session active"}"
        val centerW = footerPaint.measureText(center)
        val centerPos = (leftMargin + rightMargin) / 2f - centerW / 2f
        canvas.drawText(center, centerPos, y, footerPaint)

        val right = "Page 1 / 1"
        val rightW = footerPaint.measureText(right)
        canvas.drawText(right, rightMargin - rightW, y, footerPaint)
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E
    )
}
