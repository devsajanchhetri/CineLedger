package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.ShootRecord
import com.example.data.model.StudioBalanceSummary
import com.example.data.model.WithdrawalRecord
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
  fun format(amount: Double, currencySymbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
      minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
      maximumFractionDigits = 2
    }
    val prefix = if (currencySymbol == "रु" || currencySymbol == "AED") "$currencySymbol " else currencySymbol
    return if (amount < 0) {
      "-$prefix${formatter.format(kotlin.math.abs(amount))}"
    } else {
      "$prefix${formatter.format(amount)}"
    }
  }

  fun formatDate(millis: Long): String {
    if (millis <= 0L) return "—"
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return sdf.format(Date(millis))
  }

  fun formatShortDate(millis: Long): String {
    if (millis <= 0L) return "—"
    val sdf = SimpleDateFormat("dd MMM", Locale.US)
    return sdf.format(Date(millis))
  }
}

object StatementExporter {

  fun exportAndSharePdf(
    context: Context,
    cinematographerName: String,
    studioFilterName: String?,
    currencySymbol: String,
    totalEarned: Double,
    totalWithdrawn: Double,
    availableBalance: Double,
    studioSummaries: List<StudioBalanceSummary>,
    shoots: List<ShootRecord>,
    withdrawals: List<WithdrawalRecord>,
    onError: (String) -> Unit,
  ) {
    try {
      val pdfDocument = PdfDocument()
      val pageWidth = 595
      val pageHeight = 842
      var pageNumber = 1
      var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
      var page = pdfDocument.startPage(pageInfo)
      var canvas: Canvas = page.canvas

      val bgPaint = Paint().apply { color = Color.parseColor("#FFFFFF") }
      val headerBgPaint = Paint().apply { color = Color.parseColor("#141414") }
      val redAccentPaint = Paint().apply { color = Color.parseColor("#E50914") }
      val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
      val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B3B3B3")
        textSize = 10f
      }
      val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#141414")
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
      val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1C")
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
      val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        textSize = 9.5f
      }
      val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#18794E")
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
      val amberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B45309")
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
      val dividerPaint = Paint().apply {
        color = Color.parseColor("#E5E7EB")
        strokeWidth = 1f
      }
      val boxPaint = Paint().apply {
        color = Color.parseColor("#F9FAFB")
      }

      canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
      canvas.drawRect(0f, 0f, pageWidth.toFloat(), 88f, headerBgPaint)
      canvas.drawRect(0f, 84f, pageWidth.toFloat(), 88f, redAccentPaint)

      val scopeTitle = studioFilterName ?: "All Studios Combined"
      canvas.drawText("CINELEDGER — STUDIO RECONCILIATION STATEMENT", 36f, 38f, titlePaint)
      canvas.drawText(
        "Cinematographer: $cinematographerName   |   Scope: $scopeTitle   |   Generated: ${CurrencyFormatter.formatDate(System.currentTimeMillis())}",
        36f,
        62f,
        subHeaderPaint
      )

      var y = 114f

      // Summary Box
      canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 64f, boxPaint)
      canvas.drawText("TOTAL SHOOT EARNINGS", 52f, y + 22f, bodyPaint)
      canvas.drawText(CurrencyFormatter.format(totalEarned, currencySymbol), 52f, y + 44f, bodyBoldPaint)

      canvas.drawText("TOTAL STUDIO WITHDRAWALS", 230f, y + 22f, bodyPaint)
      canvas.drawText(CurrencyFormatter.format(totalWithdrawn, currencySymbol), 230f, y + 44f, amberPaint)

      canvas.drawText("AVAILABLE BALANCE TO COLLECT", 410f, y + 22f, bodyPaint)
      canvas.drawText(CurrencyFormatter.format(availableBalance, currencySymbol), 410f, y + 44f, greenPaint)

      y += 88f

      fun checkNewPage(neededHeight: Float) {
        if (y + neededHeight > pageHeight - 48f) {
          pdfDocument.finishPage(page)
          pageNumber++
          pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
          page = pdfDocument.startPage(pageInfo)
          canvas = page.canvas
          canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
          y = 48f
        }
      }

      if (studioFilterName == null && studioSummaries.isNotEmpty()) {
        checkNewPage(40f)
        canvas.drawText("STUDIO BALANCES SUMMARY (${studioSummaries.size})", 36f, y, sectionPaint)
        y += 16f
        canvas.drawLine(36f, y, (pageWidth - 36).toFloat(), y, dividerPaint)
        y += 16f
        for (summary in studioSummaries) {
          checkNewPage(22f)
          canvas.drawText(summary.studio.name, 36f, y, bodyBoldPaint)
          canvas.drawText("${summary.shootCount} shoots", 210f, y, bodyPaint)
          canvas.drawText("Earned: ${CurrencyFormatter.format(summary.totalEarned, currencySymbol)}", 280f, y, bodyPaint)
          canvas.drawText("Withdrawn: ${CurrencyFormatter.format(summary.totalWithdrawn, currencySymbol)}", 390f, y, bodyPaint)
          canvas.drawText("Bal: ${CurrencyFormatter.format(summary.availableBalance, currencySymbol)}", 490f, y, greenPaint)
          y += 18f
        }
        y += 14f
      }

      checkNewPage(40f)
      canvas.drawText("COMPLETED EVENT SHOOTS (${shoots.size})", 36f, y, sectionPaint)
      y += 14f
      canvas.drawLine(36f, y, (pageWidth - 36).toFloat(), y, dividerPaint)
      y += 16f

      if (shoots.isEmpty()) {
        canvas.drawText("No event shoots recorded for this selection.", 36f, y, bodyPaint)
        y += 24f
      } else {
        for (shoot in shoots.sortedByDescending { it.shootDateMillis }) {
          checkNewPage(28f)
          val dateStr = CurrencyFormatter.formatDate(shoot.shootDateMillis)
          val titleStr = shoot.title.take(30)
          val studioStr = "${shoot.studioName.take(18)} • ${shoot.category}"
          val durStr = "${if (shoot.durationValue % 1.0 == 0.0) shoot.durationValue.toInt() else shoot.durationValue} ${shoot.durationUnit}"
          canvas.drawText(dateStr, 36f, y, bodyPaint)
          canvas.drawText(titleStr, 110f, y, bodyBoldPaint)
          canvas.drawText(studioStr, 290f, y, bodyPaint)
          canvas.drawText(durStr, 430f, y, bodyPaint)
          canvas.drawText("+${CurrencyFormatter.format(shoot.earnings, currencySymbol)}", 490f, y, greenPaint)
          y += 18f
        }
        y += 14f
      }

      checkNewPage(40f)
      canvas.drawText("STUDIO WITHDRAWALS & PAYOUTS (${withdrawals.size})", 36f, y, sectionPaint)
      y += 14f
      canvas.drawLine(36f, y, (pageWidth - 36).toFloat(), y, dividerPaint)
      y += 16f

      if (withdrawals.isEmpty()) {
        canvas.drawText("No studio withdrawals recorded for this selection.", 36f, y, bodyPaint)
      } else {
        for (wd in withdrawals.sortedByDescending { it.withdrawalDateMillis }) {
          checkNewPage(28f)
          val dateStr = CurrencyFormatter.formatDate(wd.withdrawalDateMillis)
          val studioStr = wd.studioName.take(24)
          val methodStr = wd.paymentMethod
          val noteStr = wd.notes.ifBlank { "Studio withdrawal" }.take(28)
          canvas.drawText(dateStr, 36f, y, bodyPaint)
          canvas.drawText(studioStr, 110f, y, bodyBoldPaint)
          canvas.drawText(methodStr, 260f, y, bodyPaint)
          canvas.drawText(noteStr, 345f, y, bodyPaint)
          canvas.drawText("-${CurrencyFormatter.format(wd.amount, currencySymbol)}", 490f, y, amberPaint)
          y += 18f
        }
      }

      pdfDocument.finishPage(page)

      val dir = File(context.cacheDir, "statements").apply { mkdirs() }
      val safeStudio = (studioFilterName ?: "All_Studios").replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
      val pdfFile = File(dir, "CineLedger_Statement_${safeStudio}.pdf")
      FileOutputStream(pdfFile).use { out ->
        pdfDocument.writeTo(out)
      }
      pdfDocument.close()

      shareFile(
        context = context,
        file = pdfFile,
        mimeType = "application/pdf",
        chooserTitle = "Share Studio PDF Statement",
      )
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Failed to export PDF statement")
    }
  }

  fun exportAndShareCsv(
    context: Context,
    studioFilterName: String?,
    shoots: List<ShootRecord>,
    withdrawals: List<WithdrawalRecord>,
    onError: (String) -> Unit,
  ) {
    try {
      val sb = StringBuilder()
      sb.appendLine("Type,Date,Studio,Title_Or_Reference,Category_Or_Method,Location,Duration,Earned,Withdrawn,Notes")

      for (shoot in shoots.sortedByDescending { it.shootDateMillis }) {
        val dateStr = CurrencyFormatter.formatDate(shoot.shootDateMillis)
        val dur = "${shoot.durationValue} ${shoot.durationUnit}"
        sb.appendLine(
          "SHOOT_EARNING,\"$dateStr\",\"${escapeCsv(shoot.studioName)}\",\"${escapeCsv(shoot.title)}\",\"${escapeCsv(shoot.category)}\",\"${escapeCsv(shoot.location)}\",\"$dur\",${shoot.earnings},0.0,\"${escapeCsv(shoot.notes)}\""
        )
      }

      for (wd in withdrawals.sortedByDescending { it.withdrawalDateMillis }) {
        val dateStr = CurrencyFormatter.formatDate(wd.withdrawalDateMillis)
        sb.appendLine(
          "STUDIO_WITHDRAWAL,\"$dateStr\",\"${escapeCsv(wd.studioName)}\",\"Studio Withdrawal\",\"${escapeCsv(wd.paymentMethod)}\",\"\",\"\",0.0,${wd.amount},\"${escapeCsv(wd.notes)}\""
        )
      }

      val dir = File(context.cacheDir, "statements").apply { mkdirs() }
      val safeStudio = (studioFilterName ?: "All_Studios").replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
      val csvFile = File(dir, "CineLedger_Statement_${safeStudio}.csv")
      csvFile.writeText(sb.toString())

      shareFile(
        context = context,
        file = csvFile,
        mimeType = "text/csv",
        chooserTitle = "Share Studio CSV Statement",
      )
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Failed to export CSV statement")
    }
  }

  private fun escapeCsv(input: String): String {
    return input.replace("\"", "\"\"")
  }

  private fun shareFile(
    context: Context,
    file: File,
    mimeType: String,
    chooserTitle: String,
  ) {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file,
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, "CineLedger Studio Statement")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
  }
}
