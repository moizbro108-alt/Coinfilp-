package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.CoinSide
import kotlin.math.abs
import kotlin.math.cos

@Composable
fun Coin3DView(
  rotationY: Float,
  liftFraction: Float,
  isFlipping: Boolean,
  modifier: Modifier = Modifier,
  coinSize: Dp = 150.dp,
  onClick: (() -> Unit)? = null
) {
  // Idle floating animation when not flipping
  val infiniteTransition = rememberInfiniteTransition(label = "idle_float")
  val idleFloatY by infiniteTransition.animateFloat(
    initialValue = -5f,
    targetValue = 5f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "idle_float_y"
  )

  val idleScale by infiniteTransition.animateFloat(
    initialValue = 0.98f,
    targetValue = 1.02f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "idle_scale"
  )

  // Height lift in dp
  val liftDp = -liftFraction * 95f
  val currentOffsetY = if (isFlipping) liftDp else idleFloatY
  val currentScale = if (isFlipping) 1f + liftFraction * 0.25f else idleScale

  // Determine which side is facing the camera
  // Normalized angle between 0 and 360
  val normalizedAngle = ((rotationY % 360f) + 360f) % 360f
  val isHeadsFacing = normalizedAngle < 90f || normalizedAngle > 270f
  val visibleSide = if (isHeadsFacing) CoinSide.HEADS else CoinSide.TAILS

  // Edge compression factor: cos(angle)
  val rad = Math.toRadians(normalizedAngle.toDouble())
  val scaleX = abs(cos(rad)).toFloat().coerceIn(0.05f, 1f)

  Box(
    modifier = modifier
      .size(coinSize + 40.dp)
      .offset(y = currentOffsetY.dp)
      .scale(currentScale)
      .testTag("coin_3d_container"),
    contentAlignment = Alignment.Center
  ) {
    // Dynamic floor drop shadow that shrinks and fades as coin lifts
    val shadowAlpha = (1f - liftFraction * 0.7f).coerceIn(0.2f, 0.8f)
    val shadowWidth = coinSize * (1f - liftFraction * 0.4f)
    val shadowHeight = 22.dp * (1f - liftFraction * 0.4f)

    Canvas(
      modifier = Modifier
        .size(shadowWidth, shadowHeight)
        .offset(y = (coinSize / 2) + 20.dp)
    ) {
      drawOval(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.Black.copy(alpha = shadowAlpha * 0.55f),
            Color.Black.copy(alpha = shadowAlpha * 0.15f),
            Color.Transparent
          )
        )
      )
    }

    // 3D Coin Body with scaleX projection simulating realistic 3D cylinder rotation
    Box(
      modifier = Modifier
        .size(coinSize)
        .shadow(16.dp, CircleShape, spotColor = Color(0xFFFFD700).copy(alpha = 0.4f))
        .clip(CircleShape)
        .graphicsLayer {
          this.scaleX = scaleX
        }
        .then(
          if (onClick != null) {
            Modifier.clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(bounded = true, color = Color(0xFFFFD700))
            ) { onClick() }
          } else Modifier
        ),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w / 2f

        if (visibleSide == CoinSide.HEADS) {
          // --- HEADS (Royal Gold Side) ---
          // Outer Gold Rim with metallic specular highlight
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFFFFF7C2), // Light golden reflection
                Color(0xFFFFD700), // Pure Gold
                Color(0xFFDAA520), // Darker Goldenrod
                Color(0xFF996515)  // Deep Antique Gold Rim
              ),
              center = Offset(w * 0.35f, h * 0.3f),
              radius = radius * 1.2f
            ),
            radius = radius
          )

          // Inner Bevel Ring
          drawCircle(
            color = Color(0xFFFFE885),
            radius = radius * 0.88f,
            style = Stroke(width = 5.dp.toPx())
          )
          drawCircle(
            color = Color(0xFF6B4705),
            radius = radius * 0.82f,
            style = Stroke(width = 2.dp.toPx())
          )

          // Center Disc
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFFFFEE99),
                Color(0xFFFFD700),
                Color(0xFFB8860B)
              ),
              center = Offset(w * 0.4f, h * 0.35f),
              radius = radius * 0.8f
            ),
            radius = radius * 0.80f
          )

          // Crown Symbol Engraving in Center
          val crownPath = Path().apply {
            val cx = center.x
            val cy = center.y - radius * 0.05f
            val cw = radius * 0.65f
            val ch = radius * 0.45f

            moveTo(cx - cw / 2, cy + ch / 2)
            lineTo(cx - cw / 2, cy - ch / 3)
            lineTo(cx - cw / 4, cy)
            lineTo(cx, cy - ch / 2)
            lineTo(cx + cw / 4, cy)
            lineTo(cx + cw / 2, cy - ch / 3)
            lineTo(cx + cw / 2, cy + ch / 2)
            close()
          }

          drawPath(
            path = crownPath,
            brush = Brush.linearGradient(
              colors = listOf(Color(0xFFFFFBE0), Color(0xFFC79A00)),
              start = Offset(w * 0.3f, h * 0.3f),
              end = Offset(w * 0.7f, h * 0.7f)
            )
          )
          drawPath(
            path = crownPath,
            color = Color(0xFF664400),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )

          // Decorative Embossed Star above Crown
          val starCy = center.y - radius * 0.45f
          drawCircle(
            color = Color(0xFFFFF4B8),
            radius = 4.dp.toPx(),
            center = Offset(center.x, starCy)
          )

          // Decorative Coin Rim Beads
          val beadCount = 28
          val beadRadius = radius * 0.93f
          for (i in 0 until beadCount) {
            val angle = (i * (360.0 / beadCount)) * (Math.PI / 180.0)
            val bx = (center.x + beadRadius * cos(angle)).toFloat()
            val by = (center.y + beadRadius * kotlin.math.sin(angle)).toFloat()
            drawCircle(
              color = Color(0xFFFFFAEB),
              radius = 2.dp.toPx(),
              center = Offset(bx, by)
            )
          }

        } else {
          // --- TAILS (Platinum Silver Side) ---
          // Outer Platinum Silver Rim with crisp reflection
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFFFFFFFF), // Brilliant White
                Color(0xFFE2EDFF), // Ice Platinum
                Color(0xFF9FB6D8), // Rich Silver
                Color(0xFF4C6185)  // Deep Slate Edge
              ),
              center = Offset(w * 0.35f, h * 0.3f),
              radius = radius * 1.2f
            ),
            radius = radius
          )

          // Inner Silver Bevel Ring
          drawCircle(
            color = Color(0xFFFFFFFF),
            radius = radius * 0.88f,
            style = Stroke(width = 5.dp.toPx())
          )
          drawCircle(
            color = Color(0xFF334460),
            radius = radius * 0.82f,
            style = Stroke(width = 2.dp.toPx())
          )

          // Center Disc
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFFF0F5FF),
                Color(0xFFBDD2EE),
                Color(0xFF7A97C2)
              ),
              center = Offset(w * 0.4f, h * 0.35f),
              radius = radius * 0.8f
            ),
            radius = radius * 0.80f
          )

          // Large Stylized Number "1" or Laurel Shield
          val shieldPath = Path().apply {
            val cx = center.x
            val cy = center.y - radius * 0.05f
            val sw = radius * 0.55f
            val sh = radius * 0.60f

            moveTo(cx - sw / 2, cy - sh / 2)
            lineTo(cx + sw / 2, cy - sh / 2)
            lineTo(cx + sw / 2, cy + sh / 6)
            quadraticBezierTo(cx + sw / 2, cy + sh / 2, cx, cy + sh / 2)
            quadraticBezierTo(cx - sw / 2, cy + sh / 2, cx - sw / 2, cy + sh / 6)
            close()
          }

          drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
              colors = listOf(Color(0xFFFFFFFF), Color(0xFF8AA3CA)),
              start = Offset(w * 0.3f, h * 0.3f),
              end = Offset(w * 0.7f, h * 0.7f)
            )
          )
          drawPath(
            path = shieldPath,
            color = Color(0xFF223552),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
          )

          // Decorative Coin Rim Beads
          val beadCount = 28
          val beadRadius = radius * 0.93f
          for (i in 0 until beadCount) {
            val angle = (i * (360.0 / beadCount)) * (Math.PI / 180.0)
            val bx = (center.x + beadRadius * cos(angle)).toFloat()
            val by = (center.y + beadRadius * kotlin.math.sin(angle)).toFloat()
            drawCircle(
              color = Color(0xFFFFFFFF),
              radius = 2.dp.toPx(),
              center = Offset(bx, by)
            )
          }
        }
      }
    }
  }
}
