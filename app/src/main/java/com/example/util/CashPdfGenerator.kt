package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.SessionWithDetails
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CashPdfGenerator {

    private val numberFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        DecimalFormat("#,##0", symbols)
    }

    private val percentFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
            decimalSeparator = ','
        }
        DecimalFormat("0.00", symbols)
    }

    fun formatAmount(amount: Double, currency: String = "DA"): String {
        return "${numberFormat.format(amount)} $currency"
    }

    fun formatPercent(percent: Double): String {
        return "${percentFormat.format(percent)} %"
    }

    fun generatePdfFile(context: Context, sessionDetails: SessionWithDetails): File {
        val session = sessionDetails.session
        val fileName = "Recap_Caisse_${session.reference}.pdf"
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)

        val pdfDocument = PdfDocument()
        // Standard A4 at 72 DPI is 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawDocument(canvas, sessionDetails, pageWidth, pageHeight)

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    fun sharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Partager le Récapitulatif de Caisse"))
    }

    private fun drawDocument(
        canvas: Canvas,
        details: SessionWithDetails,
        pageWidth: Int,
        pageHeight: Int
    ) {
        val session = details.session
        val leftMargin = 30f
        val rightMargin = pageWidth - 30f
        val contentWidth = rightMargin - leftMargin

        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var y = 40f

        // 1. Header Title & Subtitle with Symphonix brand badge
        paint.color = Color.rgb(20, 107, 255) // Symphonix Blue
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val brandTag = "SYMPHONIX  |  " + (if (session.establishmentName.isNotBlank()) session.establishmentName else "CAISSE").uppercase()
        canvas.drawText(brandTag, leftMargin, y - 10f, paint)

        paint.color = Color.rgb(11, 46, 115) // Symphonix Deep Blue
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FICHE DE RÉCAPITULATIF DE CAISSE", leftMargin, y + 4f, paint)

        // Date pill badge on top right
        val dateText = "Date : ${session.dateText}"
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val dateWidth = paint.measureText(dateText)
        val datePillRect = RectF(rightMargin - dateWidth - 14f, y - 12f, rightMargin, y + 3f)
        val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 252, 231) // light mint green
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(datePillRect, 4f, 4f, pillBgPaint)
        paint.color = Color.rgb(22, 101, 52) // deep green
        canvas.drawText(dateText, rightMargin - dateWidth - 7f, y - 1f, paint)

        y += 13f
        // Subtitle
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Journal chronologique des décaissements et rapprochement du fond de roulement", leftMargin, y, paint)

        // Document Reference
        val refText = "Réf. Document : ${session.reference}"
        val refWidth = paint.measureText(refText)
        canvas.drawText(refText, rightMargin - refWidth, y, paint)

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
            sub = if (details.replenishments.isNotEmpty()) {
                "${details.replenishments.size} apports (+${formatAmount(details.totalReplenishments, session.currency)})"
            } else {
                "Dotation (${session.initialFundTime})"
            },
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

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Title
        textPaint.color = titleColor
        textPaint.textSize = 6.8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 7f, y + 11f, textPaint)

        // Amount
        textPaint.color = amountColor
        textPaint.textSize = 10.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(amount, x + 7f, y + 25f, textPaint)

        // Subtitle
        textPaint.color = Color.rgb(100, 116, 139)
        textPaint.textSize = 6.2f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(sub, x + 7f, y + 37f, textPaint)
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
            color = Color.rgb(37, 99, 235) // Deep Blue
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + titleBarHeight, bgPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.WHITE
        textPaint.textSize = 7.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Journal des Alimentations & Apports de Caisse en cours de journée", leftMargin + 8f, y + 11f, textPaint)

        val countText = "${details.replenishments.size} apport(s) constaté(s)"
        val countW = textPaint.measureText(countText)
        canvas.drawText(countText, rightMargin - countW - 8f, y + 11f, textPaint)

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

        textPaint.color = Color.rgb(30, 64, 175)
        textPaint.textSize = 6.8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("N°", colN, y + 10f, textPaint)
        canvas.drawText("Heure", colHeure, y + 10f, textPaint)
        canvas.drawText("Motif / Justification", colMotif, y + 10f, textPaint)
        canvas.drawText("Provenance / Emplacement source", colSource, y + 10f, textPaint)
        val mLabel = "Montant (${session.currency})"
        canvas.drawText(mLabel, colMontant - textPaint.measureText(mLabel), y + 10f, textPaint)

        y += 14f

        // Rows
        val rowHeight = 15f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        details.replenishments.forEachIndexed { index, item ->
            if (index % 2 == 1) {
                val zebraPaint = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(leftMargin, y, rightMargin, y + rowHeight, zebraPaint)
            }

            textPaint.color = Color.rgb(30, 41, 59)
            textPaint.textSize = 7f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawText(String.format("%02d", item.orderNumber), colN, y + 10.5f, textPaint)
            canvas.drawText(item.time, colHeure, y + 10.5f, textPaint)
            canvas.drawText(item.reason, colMotif, y + 10.5f, textPaint)
            canvas.drawText(item.sourceLocation, colSource, y + 10.5f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val amtText = formatAmount(item.amount, session.currency)
            canvas.drawText(amtText, colMontant - textPaint.measureText(amtText), y + 10.5f, textPaint)

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

        textPaint.color = Color.rgb(30, 58, 138)
        textPaint.textSize = 7.2f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL DES ALIMENTATIONS ENTRÉES :", colMotif, y + 10f, textPaint)

        val totalAmt = formatAmount(details.totalReplenishments, session.currency)
        canvas.drawText(totalAmt, colMontant - textPaint.measureText(totalAmt), y + 10f, textPaint)

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

        // Dark teal header matching document image
        val headerHeight = 21f
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 109, 91) // Deep Teal
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + headerHeight, headerBg)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.WHITE
        textPaint.textSize = 8.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Journal Nominatif des Décaissements Effectués", leftMargin + 8f, y + 14f, textPaint)

        textPaint.textSize = 7f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subRight = "Devise : Dinar Algérien (${session.currency}) — Pointage chronologique"
        val subRightW = textPaint.measureText(subRight)
        canvas.drawText(subRight, rightMargin - subRightW - 8f, y + 14f, textPaint)

        y += headerHeight

        // Sub-header columns
        val subHeaderHeight = 15f
        val subHeaderBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 77, 64) // Slightly darker teal
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + subHeaderHeight, subHeaderBg)

        val colN = leftMargin + 6f
        val colHeure = leftMargin + 25f
        val colDesig = leftMargin + 65f
        val colCat = leftMargin + 240f
        val colMontant = rightMargin - 52f
        val colPart = rightMargin - 8f

        textPaint.color = Color.WHITE
        textPaint.textSize = 7f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText("N°", colN, y + 10.5f, textPaint)
        canvas.drawText("Heure", colHeure, y + 10.5f, textPaint)
        canvas.drawText("Désignation / Bénéficiaire", colDesig, y + 10.5f, textPaint)
        canvas.drawText("Catégorie", colCat, y + 10.5f, textPaint)

        val mLabel = "Montant (${session.currency})"
        canvas.drawText(mLabel, colMontant - textPaint.measureText(mLabel), y + 10.5f, textPaint)

        val pLabel = "Part (%)"
        canvas.drawText(pLabel, colPart - textPaint.measureText(pLabel), y + 10.5f, textPaint)

        y += subHeaderHeight

        // Rows
        val rowHeight = 17f
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        val totalDecaisse = details.totalDisbursements

        details.disbursements.forEachIndexed { index, item ->
            // Subtle alternating tint
            if (index % 2 == 1) {
                val zebraPaint = Paint().apply {
                    color = Color.rgb(250, 252, 251)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(leftMargin, y, rightMargin, y + rowHeight, zebraPaint)
            }

            textPaint.color = Color.rgb(30, 41, 59)
            textPaint.textSize = 7.5f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            // N°
            canvas.drawText(String.format("%02d", item.orderNumber), colN, y + 11.5f, textPaint)
            // Heure
            canvas.drawText(item.time, colHeure, y + 11.5f, textPaint)
            // Désignation
            canvas.drawText(item.designation, colDesig, y + 11.5f, textPaint)

            // Catégorie Badge
            val catText = item.fullCategory
            val catBadgeW = textPaint.measureText(catText) + 8f
            val catPill = RectF(colCat - 3f, y + 3f, colCat + catBadgeW, y + rowHeight - 3f)
            val catPillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (item.parentCategory.contains("Personnel", true)) {
                    Color.rgb(219, 234, 254) // soft blue
                } else {
                    Color.rgb(209, 250, 229) // soft emerald
                }
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(catPill, 3f, 3f, catPillPaint)
            val catTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (item.parentCategory.contains("Personnel", true)) Color.rgb(30, 64, 175) else Color.rgb(6, 95, 70)
                textSize = 6.8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(catText, colCat + 1f, y + 11.5f, catTextPaint)

            // Montant
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.color = Color.rgb(15, 23, 42)
            val amtStr = formatAmount(item.amount, session.currency)
            canvas.drawText(amtStr, colMontant - textPaint.measureText(amtStr), y + 11.5f, textPaint)

            // Part %
            val part = if (totalDecaisse > 0) (item.amount / totalDecaisse) * 100.0 else 0.0
            val partStr = formatPercent(part)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(71, 85, 105)
            canvas.drawText(partStr, colPart - textPaint.measureText(partStr), y + 11.5f, textPaint)

            // Line separator
            canvas.drawLine(leftMargin, y + rowHeight, rightMargin, y + rowHeight, borderPaint)
            y += rowHeight
        }

        // Total Row matching document
        val totalRowHeight = 18f
        val totalRowBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(240, 253, 250) // Mint 50
            style = Paint.Style.FILL
        }
        canvas.drawRect(leftMargin, y, rightMargin, y + totalRowHeight, totalRowBg)

        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 7.8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val totalLabel = "TOTAL DES DÉCAISSEMENTS ENREGISTRÉS (${details.disbursements.size} OPÉRATIONS) :"
        val totalLabelW = textPaint.measureText(totalLabel)
        canvas.drawText(totalLabel, colCat - 30f, y + 12f, textPaint)

        textPaint.color = Color.rgb(180, 83, 9) // Orange brown matching sample
        val totalAmtStr = formatAmount(totalDecaisse, session.currency)
        canvas.drawText(totalAmtStr, colMontant - textPaint.measureText(totalAmtStr), y + 12f, textPaint)

        textPaint.color = Color.rgb(15, 23, 42)
        val hundredPercent = "100,00 %"
        canvas.drawText(hundredPercent, colPart - textPaint.measureText(hundredPercent), y + 12f, textPaint)

        val doubleBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 109, 91)
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(leftMargin, y + totalRowHeight, rightMargin, y + totalRowHeight, doubleBorder)

        y += totalRowHeight
        return y
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
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // --- LEFT COLUMN: Ventilation by Category ---
        var yLeft = startY
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Ventilation par Catégorie de Dépense", leftX, yLeft + 8f, paint)

        // Green line accent
        val accentLinePaint = Paint().apply {
            color = Color.rgb(0, 109, 91)
            strokeWidth = 1.5f
        }
        canvas.drawLine(leftX, yLeft + 12f, leftX + width, yLeft + 12f, accentLinePaint)

        yLeft += 20f

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        details.categoryBreakdowns.forEach { breakdown ->
            linePaint.textSize = 7.5f
            linePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            linePaint.color = Color.rgb(30, 41, 59)
            canvas.drawText(breakdown.name, leftX, yLeft + 5f, linePaint)

            val valText = "${formatAmount(breakdown.amount, session.currency)} (${formatPercent(breakdown.percentage)})"
            val valW = linePaint.measureText(valText)
            canvas.drawText(valText, leftX + width - valW, yLeft + 5f, linePaint)

            yLeft += 15f
        }

        // Green Observation box: POINTAGE PHYSIQUE DE CLÔTURE
        yLeft += 6f
        val boxHeight = 46f
        val obsRect = RectF(leftX, yLeft, leftX + width, yLeft + boxHeight)
        val obsBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(240, 253, 244)
            style = Paint.Style.FILL
        }
        val obsBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(187, 247, 208)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(obsRect, 4f, 4f, obsBg)
        canvas.drawRoundRect(obsRect, 4f, 4f, obsBorder)

        val obsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        obsPaint.color = Color.rgb(22, 101, 52)
        obsPaint.textSize = 7.5f
        obsPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val closeH = session.closingTime ?: "20h34"
        canvas.drawText("POINTAGE PHYSIQUE DE CLÔTURE (${closeH.uppercase()})", leftX + 8f, yLeft + 12f, obsPaint)

        val espText = "ESPÈCES : ${formatAmount(details.countedCash, session.currency)}"
        val espW = obsPaint.measureText(espText)
        canvas.drawText(espText, leftX + width - espW - 8f, yLeft + 12f, obsPaint)

        // Descriptive sentence
        obsPaint.color = Color.rgb(51, 65, 85)
        obsPaint.textSize = 6.8f
        obsPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val line1 = "Le comptage physique à $closeH fait ressortir ${formatAmount(details.countedCash, session.currency)} en espèces, en"
        val line2 = if (details.isBalanced) {
            "parfaite concordance avec le solde théorique calculé (${formatAmount(details.totalAvailableFund, session.currency)} − ${formatAmount(details.totalDisbursements, session.currency)})."
        } else {
            "écart de ${formatAmount(details.discrepancy, session.currency)} par rapport au solde théorique calculé."
        }
        val line3 = if (details.isBalanced) "Aucun écart de caisse constaté." else "Écart de pointage à régulariser en comptabilité."

        canvas.drawText(line1, leftX + 8f, yLeft + 23f, obsPaint)
        canvas.drawText(line2, leftX + 8f, yLeft + 32f, obsPaint)
        canvas.drawText(line3, leftX + 8f, yLeft + 41f, obsPaint)

        // --- RIGHT COLUMN: Pointage & Rapprochement de Caisse Table ---
        var yRight = startY
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Pointage & Rapprochement de Caisse", rightX, yRight + 8f, paint)

        canvas.drawLine(rightX, yRight + 12f, rightX + width, yRight + 12f, accentLinePaint)
        yRight += 20f

        val rapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
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

        // 2. Alimentations (if any)
        if (details.replenishments.isNotEmpty()) {
            drawRapprochementRow(
                canvas, rightX, yRight, width,
                "(+) Alimentations / Rallonges (${details.replenishments.size} apports)",
                "+ " + formatAmount(details.totalReplenishments, session.currency),
                isBold = false,
                isNegative = false,
                color = Color.rgb(37, 99, 235)
            )
            yRight += rapRowHeight
        }

        // 3. Décaissements enregistrés
        drawRapprochementRow(
            canvas, rightX, yRight, width,
            "(-) Décaissements enregistrés (${details.disbursements.size} opérations)",
            "- " + formatAmount(details.totalDisbursements, session.currency),
            isBold = false,
            isNegative = true,
            color = Color.rgb(180, 83, 9)
        )
        yRight += rapRowHeight

        // Divider
        canvas.drawLine(rightX, yRight, rightX + width, yRight, Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
        })
        yRight += 2f

        // 4. Solde Théorique Calculé
        drawRapprochementRow(
            canvas, rightX, yRight, width,
            "(=) Solde Théorique Calculé",
            formatAmount(details.theoreticalBalance, session.currency),
            isBold = true,
            isNegative = false
        )
        yRight += rapRowHeight + 4f

        // 5. Espèces Physiques Constatées (highlighted green bar)
        val espBox = RectF(rightX, yRight, rightX + width, yRight + 15f)
        canvas.drawRoundRect(espBox, 2f, 2f, Paint().apply {
            color = Color.rgb(220, 252, 231)
            style = Paint.Style.FILL
        })
        rapPaint.color = Color.rgb(22, 101, 52)
        rapPaint.textSize = 7.5f
        rapPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Espèces Physiques Constatées (${closeH}) :", rightX + 6f, yRight + 11f, rapPaint)
        val espAmt = formatAmount(details.countedCash, session.currency)
        val espAmtW = rapPaint.measureText(espAmt)
        canvas.drawText(espAmt, rightX + width - espAmtW - 6f, yRight + 11f, rapPaint)

        yRight += 19f

        // 6. Écart de pointage
        val ecartLabel = if (details.isBalanced) "Écart de pointage (Concordance parfaite) :" else "Écart de pointage constaté :"
        val ecartValStr = if (details.isBalanced) "0 ${session.currency}" else formatAmount(details.discrepancy, session.currency)
        rapPaint.color = if (details.isBalanced) Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
        canvas.drawText(ecartLabel, rightX + 6f, yRight + 10f, rapPaint)
        val ecartValW = rapPaint.measureText(ecartValStr)
        canvas.drawText(ecartValStr, rightX + width - ecartValW - 6f, yRight + 10f, rapPaint)
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
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = 7.2f
            typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
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

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Box 1: ÉTABLI PAR (RESPONSABLE DE CAISSE)
        val box1 = RectF(leftMargin, startY, leftMargin + colWidth, startY + height)
        canvas.drawRect(box1, bgPaint)
        canvas.drawRect(box1, borderPaint)

        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 6.8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ÉTABLI PAR (${details.session.responsibleName.uppercase()}) :", leftMargin + 8f, startY + 12f, textPaint)

        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 6.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Signature & Date (${details.session.dateText})", leftMargin + 8f, startY + 40f, textPaint)

        // Box 2: VÉRIFIÉ & VALIDÉ PAR (DIRECTION / GÉRANCE)
        val rightX = rightMargin - colWidth
        val box2 = RectF(rightX, startY, rightMargin, startY + height)
        canvas.drawRect(box2, bgPaint)
        canvas.drawRect(box2, borderPaint)

        textPaint.color = Color.rgb(71, 85, 105)
        textPaint.textSize = 6.8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VÉRIFIÉ & VALIDÉ PAR (${details.session.managerName.uppercase()}) :", rightX + 8f, startY + 12f, textPaint)

        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.textSize = 6.5f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Signature & Cachet", rightX + 8f, startY + 40f, textPaint)
    }

    private fun drawFooter(
        canvas: Canvas,
        details: SessionWithDetails,
        leftMargin: Float,
        y: Float,
        rightMargin: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val sep = " — "
        val left = "SYMPHONIX Caisse$sep${details.session.establishmentName}"
        canvas.drawText(left, leftMargin, y, paint)

        val center = "Arrêté du ${details.session.dateText}$sep${if (details.session.isClosed) "Clôture journalière" else "Session active"}"
        val centerW = paint.measureText(center)
        val centerPos = (leftMargin + rightMargin) / 2f - centerW / 2f
        canvas.drawText(center, centerPos, y, paint)

        val right = "Page 1 / 1"
        val rightW = paint.measureText(right)
        canvas.drawText(right, rightMargin - rightW, y, paint)
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E
    )
}
