package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationType
import com.example.model.ThemeMode
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.CoinFlipViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsAndCloudDialog(
  viewModel: CoinFlipViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val themeMode by viewModel.themeMode.collectAsState()
  val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
  val lastSynced by viewModel.lastSyncedTimestamp.collectAsState()
  val isSyncing by viewModel.isCloudSyncing.collectAsState()

  var showImportField by remember { mutableStateOf(false) }
  var importCodeText by remember { mutableStateOf("") }
  var copiedToast by remember { mutableStateOf(false) }

  val syncDateStr = remember(lastSynced) {
    SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(lastSynced))
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Done", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CloudSync,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Settings & Cloud Sync",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Theme Mode Selector
        Text(
          text = "APPEARANCE & THEME",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val modes = listOf(
            Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
            Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
            Triple(ThemeMode.SYSTEM, "Auto", Icons.Default.PhoneAndroid)
          )

          modes.forEach { (mode, label, icon) ->
            val isSelected = themeMode == mode
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                  width = 1.dp,
                  color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { viewModel.setThemeMode(mode) }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = label,
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Haptic Feedback Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(text = "Haptic Vibrations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(text = "Coin spin & victory haptics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }

          Switch(
            checked = hapticsEnabled,
            onCheckedChange = { viewModel.setHapticsEnabled(it) },
            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldPrimary.copy(alpha = 0.3f))
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Push Notifications for User Engagement
        Text(
          text = "PUSH NOTIFICATIONS & ENGAGEMENT",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Engagement Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                  text = "Match challenges, live player invites, and victory alerts",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = GoldPrimary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trigger Push Notification Button
            OutlinedButton(
              onClick = { viewModel.sendTestPushNotification(NotificationType.GAME_CHALLENGE) },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("send_test_push_btn")
            ) {
              Text("Send Sample Push Notification", fontSize = 11.sp, fontFamily = ChakraPetchFont)
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Cloud Synchronization Section
        Text(
          text = "CLOUD SYNCHRONIZATION",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "Cloud Sync Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(
                  text = "Last synced: $syncDateStr",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Button(
                onClick = { viewModel.triggerCloudSync() },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF221600)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("sync_now_button")
              ) {
                Text(text = if (isSyncing) "Syncing..." else "Sync Now", fontSize = 11.sp, fontFamily = ChakraPetchFont)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Export Cloud Token
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  val code = viewModel.exportCloudCode()
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("CoinFlipSync", code))
                  copiedToast = true
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("copy_sync_code_btn")
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (copiedToast) "Copied!" else "Export Sync", fontSize = 11.sp, fontFamily = ChakraPetchFont)
              }

              OutlinedButton(
                onClick = { showImportField = !showImportField },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("toggle_import_btn")
              ) {
                Text(text = if (showImportField) "Hide" else "Import Sync", fontSize = 11.sp, fontFamily = ChakraPetchFont)
              }
            }

            if (showImportField) {
              Spacer(modifier = Modifier.height(10.dp))
              OutlinedTextField(
                value = importCodeText,
                onValueChange = { importCodeText = it },
                label = { Text("Paste Cloud Sync Code") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Button(
                onClick = {
                  if (importCodeText.isNotBlank()) {
                    viewModel.importCloudCode(importCodeText)
                    importCodeText = ""
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF221600)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("import_sync_submit_btn")
              ) {
                Text("Restore Data from Sync Code", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }

        // Download App Button
        Button(
          onClick = {
            onDismiss()
            viewModel.openDownload(true)
          },
          colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF221600)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().height(46.dp).testTag("settings_open_download_btn")
        ) {
          Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Download App (APK / ZIP)", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        // Cross-platform compatibility info
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(10.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "📱 Cross-Platform Architecture Ready",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "This app's data schemas, 3D physics state, and Cloud Sync tokens match cross-platform React Native / Flutter models for full interoperability across iOS, Android, and Web.",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  )
}
