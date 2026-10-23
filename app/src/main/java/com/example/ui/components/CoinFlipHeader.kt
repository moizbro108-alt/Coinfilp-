package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.User
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary

@Composable
fun CoinFlipHeader(
  user: User?,
  isSyncing: Boolean,
  unreadCount: Int,
  onOpenNotifications: () -> Unit,
  onOpenDownload: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenCloudSync: () -> Unit,
  onOpenAuth: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
  val syncAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sync_spin_angle"
  )

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // App Logo + Name + Cloud status
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { onOpenAuth() }
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(GoldPrimary, Color(0xFFC79A00))),
                shape = CircleShape
              )
          ) {
            Image(
              painter = painterResource(id = R.drawable.coin_flip_logo),
              contentDescription = "Coin Flip Logo",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "COIN FLIP",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = MaterialTheme.colorScheme.onSurface,
              letterSpacing = 0.5.sp
            )

            // Cloud Sync status badge
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onOpenCloudSync() }
                .padding(vertical = 1.dp)
            ) {
              Icon(
                imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                contentDescription = "Cloud Status",
                tint = if (isSyncing) GoldPrimary else Color(0xFF10B981),
                modifier = Modifier
                  .size(12.dp)
                  .then(if (isSyncing) Modifier.rotate(syncAngle) else Modifier)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isSyncing) "Syncing..." else "Cloud Synced",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSyncing) GoldPrimary else Color(0xFF10B981),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        // Actions: Balance Pill + Notification Bell + Settings
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Balance pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(
                brush = Brush.horizontalGradient(
                  listOf(Color(0xFF2A2408), Color(0xFF382F05))
                )
              )
              .border(
                width = 1.dp,
                color = GoldPrimary.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
              )
              .clickable { onOpenAuth() }
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("balance_pill_header")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "🪙",
                fontSize = 13.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = String.format("%,d", user?.balance ?: 0L),
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                fontSize = 14.sp
              )
            }
          }

          // Notification icon with badge
          IconButton(
            onClick = onOpenNotifications,
            modifier = Modifier
              .size(42.dp)
              .testTag("notification_bell_button")
          ) {
            BadgedBox(
              badge = {
                if (unreadCount > 0) {
                  Badge(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                  ) {
                    Text(text = if (unreadCount > 9) "9+" else "$unreadCount")
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Download App & APK button
          IconButton(
            onClick = onOpenDownload,
            modifier = Modifier
              .size(42.dp)
              .testTag("download_app_header_button")
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = "Download App",
              tint = GoldPrimary
            )
          }

          // Settings button
          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
              .size(42.dp)
              .testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
