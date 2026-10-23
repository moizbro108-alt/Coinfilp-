package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.WinGreen
import com.example.viewmodel.CoinFlipViewModel

@Composable
fun DownloadAppDialog(
  viewModel: CoinFlipViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var copiedText by remember { mutableStateOf<String?>(null) }

  val apkUrl = "https://ais-pre-u35f5s5cktshrxpm3b32ov-479158337887.asia-east1.run.app/CoinFlip.apk"
  val zipUrl = "https://ais-pre-u35f5s5cktshrxpm3b32ov-479158337887.asia-east1.run.app/coin-flip-app.zip"
  val appShareUrl = "https://ais-pre-u35f5s5cktshrxpm3b32ov-479158337887.asia-east1.run.app"

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
          imageVector = Icons.Default.Download,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Download App & Export",
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // App Identity Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
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

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Coin Flip Mobile App",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "v1.0.0 • Native Android & Jetpack Compose",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
            Surface(
              color = WinGreen.copy(alpha = 0.15f),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Text(
                text = "✓ Ready to Install • 23 MB APK",
                fontSize = 10.sp,
                color = WinGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Section 1: Download APK
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color(0xFF3DDC84), modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Android APK Package",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Install directly on any Android phone or tablet without needing the app store.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
                  context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = GoldPrimary,
                  contentColor = Color(0xFF221600)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.3f).testTag("download_apk_btn")
              ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download APK", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("CoinFlip APK URL", apkUrl))
                  copiedText = "APK Link copied!"
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("copy_apk_link_btn")
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Link", fontSize = 11.sp, fontFamily = ChakraPetchFont)
              }
            }
          }
        }

        // Section 2: Download Project Source (ZIP)
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Full Project Source (ZIP)",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Complete source code, 3D physics canvas, vector assets, and Gradle build configurations.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(zipUrl))
                  context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.3f).testTag("download_zip_btn")
              ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download ZIP", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("CoinFlip ZIP URL", zipUrl))
                  copiedText = "ZIP Link copied!"
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("copy_zip_link_btn")
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Link", fontSize = 11.sp, fontFamily = ChakraPetchFont)
              }
            }
          }
        }

        // Section 3: Share App
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "Share with Friends", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                  text = "Invite friends to download the app and challenge your lobby",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Button(
                onClick = {
                  val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                      Intent.EXTRA_TEXT,
                      "Play Coin Flip with me! Download the app: $appShareUrl or direct APK: $apkUrl"
                    )
                  }
                  context.startActivity(Intent.createChooser(shareIntent, "Share Coin Flip App"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF221600)),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share", fontSize = 11.sp, fontFamily = ChakraPetchFont)
              }
            }
          }
        }

        if (copiedText != null) {
          Text(
            text = copiedText!!,
            color = WinGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
          )
        }

        // Quick Installation Guide
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(10.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "📌 How to install on your phone:",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "1. Tap 'Download APK' to save the file on your device.\n2. Tap the downloaded file and select 'Install'.\n3. If prompted, toggle 'Allow from this source'.",
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
