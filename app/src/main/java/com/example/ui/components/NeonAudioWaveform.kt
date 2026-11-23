package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import kotlin.math.sin

/**
 * Animated Neon Sound Waveform Canvas:
 * Renders pulsating harmonic neon waves in Cyan, Magenta, and Amber
 * that react to speech recognition, thinking, and speaking states.
 */
@Composable
fun NeonAudioWaveform(
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isListening -> 1000
                    isSpeaking -> 1200
                    isThinking -> 1600
                    else -> 3000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val amplitudeMultiplier = when {
        isListening -> 1.35f
        isSpeaking -> 1.15f
        isThinking -> 0.75f
        else -> 0.35f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Wave 1: Neon Cyan Primary
        drawWave(
            width = width,
            midY = midY,
            phase = phase,
            frequency = 3.5f,
            amplitude = 22f * amplitudeMultiplier,
            brush = Brush.horizontalGradient(
                listOf(NeonCyan.copy(alpha = 0.2f), NeonCyan, NeonCyan.copy(alpha = 0.2f))
            ),
            strokeWidth = 3.dp.toPx()
        )

        // Wave 2: Neon Magenta / Pink Harmonic
        drawWave(
            width = width,
            midY = midY,
            phase = phase + 1.2f,
            frequency = 4.2f,
            amplitude = 17f * amplitudeMultiplier,
            brush = Brush.horizontalGradient(
                listOf(NeonPink.copy(alpha = 0.2f), NeonPink, NeonPink.copy(alpha = 0.2f))
            ),
            strokeWidth = 2.4.dp.toPx()
        )

        // Wave 3: Neon Amber Subtle Glow
        drawWave(
            width = width,
            midY = midY,
            phase = phase + 2.4f,
            frequency = 2.8f,
            amplitude = 12f * amplitudeMultiplier,
            brush = Brush.horizontalGradient(
                listOf(NeonAmber.copy(alpha = 0.1f), NeonAmber.copy(alpha = 0.8f), NeonAmber.copy(alpha = 0.1f))
            ),
            strokeWidth = 1.8.dp.toPx()
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWave(
    width: Float,
    midY: Float,
    phase: Float,
    frequency: Float,
    amplitude: Float,
    brush: Brush,
    strokeWidth: Float
) {
    val path = Path()
    val step = 4f
    var first = true

    var x = 0f
    while (x <= width) {
        // Normal distribution window to taper wave at left and right edges
        val progress = x / width
        val envelope = (sin(progress * Math.PI.toFloat())).coerceIn(0f, 1f)
        val y = midY + sin((progress * frequency * 2 * Math.PI.toFloat()) + phase) * amplitude * envelope

        if (first) {
            path.moveTo(x, y)
            first = false
        } else {
            path.lineTo(x, y)
        }
        x += step
    }

    drawPath(
        path = path,
        brush = brush,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
