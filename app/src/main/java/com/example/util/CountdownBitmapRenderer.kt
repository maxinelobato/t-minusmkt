package com.example.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import com.example.data.model.TaskEntity
import java.util.Locale

/**
 * Renders crisp high-resolution Bitmaps matching the Soft-Neumorphic Tailwind Zinc
 * Light (#E4E4E7) and Dark (#27272A) Countdown Lock Screen & Share Card designs,
 * displaying ALL tasks in chronological order alongside the live countdown timer.
 */
object CountdownBitmapRenderer {

    /**
     * Generates a 1080x1920 Lock Screen Wallpaper Bitmap with the top 20% left completely
     * clean for the Android system clock and date, and the Neumorphic Countdown Card centered.
     */
    fun renderLockScreenWallpaperBitmap(
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long = 0L,
        minutesRemaining: Long = 0L,
        secondsRemaining: Long = 0L,
        isPt: Boolean,
        isDark: Boolean
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = if (isDark) Color.parseColor("#27272A") else Color.parseColor("#E4E4E7")
        canvas.drawColor(bgColor)

        // Top 20% (~384px) intentionally clean for Android system lock screen clock & date
        drawCountdownCardOnCanvas(
            canvas = canvas,
            cardLeft = 88f,
            cardTop = 430f,
            cardRight = width - 88f,
            cardBottom = 1690f,
            tasks = tasks,
            daysRemaining = daysRemaining,
            hoursRemaining = hoursRemaining,
            minutesRemaining = minutesRemaining,
            secondsRemaining = secondsRemaining,
            isPt = isPt,
            isDark = isDark
        )

        return bitmap
    }

    /**
     * Generates a 1080x1450 social/messaging shareable Countdown Card Bitmap showing all milestones.
     */
    fun renderShareCardBitmap(
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long = 0L,
        minutesRemaining: Long = 0L,
        secondsRemaining: Long = 0L,
        isPt: Boolean,
        isDark: Boolean
    ): Bitmap {
        val width = 1080
        val height = 1480
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = if (isDark) Color.parseColor("#27272A") else Color.parseColor("#E4E4E7")
        canvas.drawColor(bgColor)

        // Brand wordmark at top
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 48f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = if (isDark) Color.WHITE else Color.parseColor("#18181B")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("T-Minus Mkt", width / 2f, 108f, brandPaint)

        drawCountdownCardOnCanvas(
            canvas = canvas,
            cardLeft = 80f,
            cardTop = 160f,
            cardRight = width - 80f,
            cardBottom = 1380f,
            tasks = tasks,
            daysRemaining = daysRemaining,
            hoursRemaining = hoursRemaining,
            minutesRemaining = minutesRemaining,
            secondsRemaining = secondsRemaining,
            isPt = isPt,
            isDark = isDark
        )

        return bitmap
    }

    /**
     * Applies the rendered Countdown Bitmap to the Android Lock Screen Wallpaper (Android N+)
     * and falls back cleanly to the native image share chooser if unsupported on the device.
     */
    fun applyLockScreenWallpaperOrShare(
        context: Context,
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long = 0L,
        minutesRemaining: Long = 0L,
        secondsRemaining: Long = 0L,
        isPt: Boolean,
        isDark: Boolean
    ): Boolean {
        val bitmap = renderLockScreenWallpaperBitmap(
            tasks = tasks,
            daysRemaining = daysRemaining,
            hoursRemaining = hoursRemaining,
            minutesRemaining = minutesRemaining,
            secondsRemaining = secondsRemaining,
            isPt = isPt,
            isDark = isDark
        )
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            true
        } catch (_: Exception) {
            ShareHelper.shareBitmap(
                context = context,
                bitmap = bitmap,
                title = if (isPt) "Wallpaper Contagem Regressiva T-Minus Mkt" else "T-Minus Mkt Countdown Wallpaper"
            )
            false
        }
    }

    /**
     * Silently updates ONLY the Android System Lock Screen wallpaper (FLAG_LOCK on Android N+)
     * whenever tasks, language, or theme change — never alters the Home Screen wallpaper.
     */
    fun applyLockScreenWallpaperSilently(
        context: Context,
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long = 0L,
        minutesRemaining: Long = 0L,
        secondsRemaining: Long = 0L,
        isPt: Boolean,
        isDark: Boolean
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (!wallpaperManager.isSetWallpaperAllowed) return false
            val bitmap = renderLockScreenWallpaperBitmap(
                tasks = tasks,
                daysRemaining = daysRemaining,
                hoursRemaining = hoursRemaining,
                minutesRemaining = minutesRemaining,
                secondsRemaining = secondsRemaining,
                isPt = isPt,
                isDark = isDark
            )
            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            bitmap.recycle()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Formats a clean plain-text marketing launch checklist summary for instant messaging / email sharing,
     * including all milestones and remaining countdown time.
     */
    fun buildShareTextSummary(
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long = 0L,
        minutesRemaining: Long = 0L,
        secondsRemaining: Long = 0L,
        isPt: Boolean
    ): String {
        val timerPart = String.format(
            Locale.US,
            "%02dh : %02dm : %02ds",
            hoursRemaining.coerceAtLeast(0L),
            minutesRemaining.coerceAtLeast(0L),
            secondsRemaining.coerceAtLeast(0L)
        )
        val header = if (isPt) {
            "T-Minus Mkt • Planejamento de Marketing\nContagem Regressiva: T-$daysRemaining DIAS ($timerPart)\n"
        } else {
            "T-Minus Mkt • Marketing Planning\nCountdown: T-$daysRemaining DAYS ($timerPart)\n"
        }
        val lines = tasks.joinToString("\n") { task ->
            val mark = if (task.isCompleted) "[✓]" else "[ ]"
            val timeline = (if (isPt) task.timelinePt else task.timelineEn).trimEnd(':')
            val title = if (isPt) task.titlePt else task.titleEn
            "$mark $timeline — $title"
        }
        return "$header\n$lines"
    }

    private fun drawCountdownCardOnCanvas(
        canvas: Canvas,
        cardLeft: Float,
        cardTop: Float,
        cardRight: Float,
        cardBottom: Float,
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long,
        minutesRemaining: Long,
        secondsRemaining: Long,
        isPt: Boolean,
        isDark: Boolean
    ) {
        val cardRadius = 56f
        val surfaceColor = if (isDark) Color.parseColor("#2C2C30") else Color.parseColor("#EBEBEE")
        val lightShadow = if (isDark) Color.parseColor("#38383E") else Color.WHITE
        val darkShadow = if (isDark) Color.parseColor("#141417") else Color.parseColor("#C2C2CB")
        val textPrimary = if (isDark) Color.parseColor("#FAFAFA") else Color.parseColor("#18181B")
        val textSecondary = if (isDark) Color.parseColor("#A1A1AA") else Color.parseColor("#52525B")

        val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)

        // Outer Neumorphic Shadows
        val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightShadow
            maskFilter = BlurMaskFilter(26f, BlurMaskFilter.Blur.NORMAL)
        }
        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkShadow
            maskFilter = BlurMaskFilter(28f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(cardRect.left - 14f, cardRect.top - 14f, cardRect.right - 14f, cardRect.bottom - 14f),
            cardRadius, cardRadius, lightPaint
        )
        canvas.drawRoundRect(
            RectF(cardRect.left + 16f, cardRect.top + 16f, cardRect.right + 16f, cardRect.bottom + 16f),
            cardRadius, cardRadius, darkPaint
        )

        val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = surfaceColor }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, surfacePaint)

        val centerX = (cardLeft + cardRight) / 2f

        // Title: CONTAGEM REGRESSIVA / COUNTDOWN
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimary
            textSize = 52f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isPt) "CONTAGEM REGRESSIVA" else "COUNTDOWN",
            centerX,
            cardTop + 104f,
            titlePaint
        )

        // Subtitle: T-MINUS MKT • PLANEJAMENTO DE MARKETING / MARKETING PLANNING
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = 27f
            letterSpacing = 0.05f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isPt) "T-MINUS MKT • PLANEJAMENTO DE MARKETING" else "T-MINUS MKT • MARKETING PLANNING",
            centerX,
            cardTop + 158f,
            subtitlePaint
        )

        // Inset Glowing Cyan Badge ("T-XX DIAS" / "T-XX DAYS")
        val badgeW = 460f
        val badgeH = 112f
        val badgeRect = RectF(
            centerX - badgeW / 2f,
            cardTop + 196f,
            centerX + badgeW / 2f,
            cardTop + 196f + badgeH
        )
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#232327") else Color.parseColor("#E3E3E8")
        }
        canvas.drawRoundRect(badgeRect, 32f, 32f, badgeBgPaint)

        val badgeLabel = if (isPt) "T-$daysRemaining DIAS" else "T-$daysRemaining DAYS"
        val glowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            textSize = 64f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            maskFilter = BlurMaskFilter(22f, BlurMaskFilter.Blur.NORMAL)
        }
        val crispTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22D3EE")
            textSize = 64f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val badgeBaseline = badgeRect.centerY() + 22f
        canvas.drawText(badgeLabel, centerX, badgeBaseline, glowTextPaint)
        canvas.drawText(badgeLabel, centerX, badgeBaseline, crispTextPaint)

        // Live remaining time beneath badge ("HHh : MMm : SSs")
        val timerText = String.format(
            Locale.US,
            "%02dh : %02dm : %02ds",
            hoursRemaining.coerceAtLeast(0L),
            minutesRemaining.coerceAtLeast(0L),
            secondsRemaining.coerceAtLeast(0L)
        )
        val timerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#22D3EE") else Color.parseColor("#0891B2")
            textSize = 36f
            letterSpacing = 0.08f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(timerText, centerX, badgeRect.bottom + 54f, timerPaint)

        // Nested Inner Milestones Card showing ALL tasks
        val innerRect = RectF(cardLeft + 34f, badgeRect.bottom + 88f, cardRight - 34f, cardBottom - 38f)
        canvas.drawRoundRect(
            RectF(innerRect.left + 8f, innerRect.top + 8f, innerRect.right + 8f, innerRect.bottom + 8f),
            42f, 42f, darkPaint
        )
        canvas.drawRoundRect(innerRect, 42f, 42f, surfacePaint)

        val visibleTasks = tasks.ifEmpty { emptyList() }
        if (visibleTasks.isEmpty()) return

        val rowCount = visibleTasks.size
        val availableH = innerRect.height() - 44f
        val rowHeight = availableH / rowCount.coerceAtLeast(1)
        val textScale = if (rowCount <= 5) 1f else (5f / rowCount).coerceIn(0.72f, 1f)

        val timelinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimary
            textSize = 31f * textScale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondary
            textSize = 28f * textScale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val boxStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#71717A")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val checkFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22D3EE")
            style = Paint.Style.FILL
        }

        visibleTasks.forEachIndexed { idx, task ->
            val rowTop = innerRect.top + 22f + idx * rowHeight
            val rowMidY = rowTop + rowHeight / 2f

            // Square Checkbox
            val cbSize = 42f * textScale
            val cbRect = RectF(
                innerRect.left + 38f,
                rowMidY - cbSize / 2f,
                innerRect.left + 38f + cbSize,
                rowMidY + cbSize / 2f
            )
            if (task.isCompleted) {
                canvas.drawRoundRect(cbRect, 9f, 9f, checkFillPaint)
            } else {
                canvas.drawRoundRect(cbRect, 9f, 9f, boxStrokePaint)
            }

            val timeline = (if (isPt) task.timelinePt else task.timelineEn).trimEnd(':').trim()
            val title = if (isPt) task.titlePt else task.titleEn
            val textX = innerRect.left + 108f
            canvas.drawText(timeline.take(36), textX, rowMidY - 6f * textScale, timelinePaint)
            canvas.drawText(title.take(36), textX, rowMidY + 30f * textScale, descPaint)

            // Colored right-side status ring
            val accentHex = when (task.iconType.lowercase()) {
                "search", "magnifyingglass" -> "#22D3EE"
                "article", "list" -> "#C084FC"
                "megaphone" -> "#FACC15"
                "checkcircle", "check" -> "#F472B6"
                else -> "#4ADE80"
            }
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(accentHex)
                style = Paint.Style.STROKE
                strokeWidth = 5f
            }
            canvas.drawCircle(innerRect.right - 56f, rowMidY, 20f * textScale, dotPaint)
        }
    }
}
