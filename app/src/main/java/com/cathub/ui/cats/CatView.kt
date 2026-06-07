package com.cathub.ui.cats

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cathub.R
import com.cathub.data.model.CatState
import com.cathub.data.model.FamilyMember
import kotlin.math.sin

/**
 * CatView — renders a family member's cat avatar with animations.
 *
 * Uses Compose Canvas to draw the sprite parts (body, head, paw, eyes)
 * with programmatic animations for different states.
 */
@Composable
fun CatView(
    member: FamilyMember,
    catState: CatState,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
) {
    val context = LocalContext.current

    // Load sprite bitmaps from resources
    val spriteResId = when (member.id) {
        "will" -> R.drawable.cats_will_body
        "lucy" -> R.drawable.cats_lucy_body
        "imogen" -> R.drawable.cats_imogen_body
        else -> R.drawable.cats_will_body
    }

    val headResId = when (member.id) {
        "will" -> R.drawable.cats_will_head
        "lucy" -> R.drawable.cats_lucy_head
        "imogen" -> R.drawable.cats_imogen_head
        else -> R.drawable.cats_will_head
    }

    val pawResId = when (member.id) {
        "will" -> R.drawable.cats_will_paw
        "lucy" -> R.drawable.cats_lucy_paw
        "imogen" -> R.drawable.cats_imogen_paw
        else -> R.drawable.cats_will_paw
    }

    val bodyBitmap = ImageBitmap.imageResource(id = spriteResId)
    val headBitmap = ImageBitmap.imageResource(id = headResId)
    val pawBitmap = ImageBitmap.imageResource(id = pawResId)

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "cat_${member.id}")

    // Breathing animation (subtle vertical movement)
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    // Eye blink animation
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3500
                0f at 3600
                1f at 3700
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "blink",
    )

    // Tail wag animation (rotation)
    val tailWag by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tailWag",
    )

    // Paw walk animation (only when walking)
    val pawOffset by animateFloatAsState(
        targetValue = if (catState == CatState.WALKING) 4f else 0f,
        animationSpec = tween(300),
        label = "paw",
    )

    // Excited bounce animation
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (catState == CatState.EXCITED) -8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounce",
    )

    // Sleep Z animation
    val sleepZ by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sleepZ",
    )

    // Sad rain drop animation
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rain",
    )

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Cat sprite canvas
        Canvas(
            modifier = Modifier
                .size(96.dp)
                .clickable { onTap() },
        ) {
            val scale = size.width / 32f // Scale from 32px base to actual size
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Apply state-specific transforms
            val offsetY = when (catState) {
                CatState.SLEEPING -> breathe * scale
                CatState.EXCITED -> bounce * scale
                else -> breathe * scale * 0.5f
            }

            // Draw body
            drawImage(
                image = bodyBitmap,
                dstOffset = IntOffset(
                    (centerX - 12 * scale).toInt(),
                    (centerY - 8 * scale + offsetY).toInt(),
                ),
                dstSize = IntSize((24 * scale).toInt(), (20 * scale).toInt()),
            )

            // Draw head with slight rotation for idle
            rotate(
                degrees = if (catState == CatState.THINKING) 5f else 0f,
                pivot = Offset(centerX, centerY - 12 * scale + offsetY),
            ) {
                drawImage(
                    image = headBitmap,
                    dstOffset = IntOffset(
                        (centerX - 10 * scale).toInt(),
                        (centerY - 20 * scale + offsetY).toInt(),
                    ),
                    dstSize = IntSize((20 * scale).toInt(), (16 * scale).toInt()),
                    alpha = if (catState == CatState.SLEEPING) 0.8f else 1f,
                )
            }

            // Draw eyes with blink
            if (catState != CatState.SLEEPING) {
                drawCircle(
                    color = Color.Black.copy(alpha = blinkAlpha),
                    radius = 2 * scale,
                    center = Offset(
                        centerX - 4 * scale,
                        centerY - 16 * scale + offsetY,
                    ),
                )
                drawCircle(
                    color = Color.Black.copy(alpha = blinkAlpha),
                    radius = 2 * scale,
                    center = Offset(
                        centerX + 4 * scale,
                        centerY - 16 * scale + offsetY,
                    ),
                )
            }

            // Draw paw
            drawImage(
                image = pawBitmap,
                dstOffset = IntOffset(
                    (centerX - 6 * scale + pawOffset).toInt(),
                    (centerY + 8 * scale + offsetY).toInt(),
                ),
                dstSize = IntSize((8 * scale).toInt(), (6 * scale).toInt()),
            )

            // Draw state-specific effects
            when (catState) {
                CatState.SLEEPING -> {
                    // Z's floating up
                    drawText("Z", centerX + 8 * scale, centerY - 24 * scale + sleepZ * -20 * scale)
                }
                CatState.SAD -> {
                    // Rain drops
                    for (i in 0..3) {
                        drawCircle(
                            color = Color(0xFF64B5F6).copy(alpha = 0.6f),
                            radius = 1.5f * scale,
                            center = Offset(
                                centerX - 8 * scale + i * 6 * scale,
                                centerY - 20 * scale + rainOffset * scale,
                            ),
                        )
                    }
                }
                CatState.EXCITED -> {
                    // Sparkles
                    for (i in 0..4) {
                        val angle = i * 72f
                        val sparkleX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * 14 * scale
                        val sparkleY = centerY - 10 * scale + sin(Math.toRadians(angle.toDouble())).toFloat() * 14 * scale
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = 0.8f),
                            radius = 2 * scale,
                            center = Offset(sparkleX, sparkleY),
                        )
                    }
                }
                CatState.LISTENING -> {
                    // Green glow around cat when listening
                    drawCircle(
                        color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                        radius = 16 * scale,
                        center = Offset(centerX, centerY - 8 * scale + offsetY),
                    )
                    // Ears up indicator
                    drawCircle(
                        color = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        radius = 2 * scale,
                        center = Offset(centerX - 6 * scale, centerY - 22 * scale + offsetY),
                    )
                    drawCircle(
                        color = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        radius = 2 * scale,
                        center = Offset(centerX + 6 * scale, centerY - 22 * scale + offsetY),
                    )
                }
                CatState.TALKING -> {
                    // Speech bubble indicator
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 3 * scale,
                        center = Offset(centerX + 12 * scale, centerY - 18 * scale + offsetY),
                    )
                }
                CatState.THINKING -> {
                    // Thought dots
                    for (i in 0..2) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f + i * 0.15f),
                            radius = (1 + i) * scale,
                            center = Offset(
                                centerX + 10 * scale + i * 4 * scale,
                                centerY - 22 * scale - i * 4 * scale + offsetY,
                            ),
                        )
                    }
                }
                else -> {}
            }
        }

        // Name tag
        Text(
            text = member.displayName,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )

        // State label
        Text(
            text = catState.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Helper to draw text on Canvas (simplified).
 */
private fun DrawScope.drawText(text: String, x: Float, y: Float) {
    // For now, draw a simple circle placeholder
    // Real implementation would use drawText with Paint
    drawCircle(
        color = Color(0xFF9E9E9E).copy(alpha = 0.7f),
        radius = 4.dp.toPx(),
        center = Offset(x, y),
    )
}

/**
 * Clickable modifier (simplified).
 */
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this // TODO: Implement proper clickable
}

private fun cos(radians: Double): Double = kotlin.math.cos(radians)

// Add LISTENING state handling - insert after EXCITED block in when statement
