package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.WinGreen
import com.example.viewmodel.CoinFlipViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
  viewModel: CoinFlipViewModel,
  modifier: Modifier = Modifier
) {
  val globalStats by viewModel.globalStats.collectAsState()
  val users by viewModel.users.collectAsState()
  val depositAlerts by viewModel.depositAlerts.collectAsState()
  val giveaways by viewModel.giveaways.collectAsState()
  val whatsappBuyLink by viewModel.whatsappBuyLink.collectAsState()

  val nonAdminPlayers = remember(users) { users.filter { it.role != "admin" } }

  var grantTargetText by remember { mutableStateOf("") }
  var grantCoinsText by remember { mutableStateOf("1000") }
  var grantVipDaysText by remember { mutableStateOf("7") }

  var newBuyLinkText by remember(whatsappBuyLink) { mutableStateOf(whatsappBuyLink) }

  var newGiveawayTitle by remember { mutableStateOf("") }
  var newGiveawayDesc by remember { mutableStateOf("") }
  var newGiveawayLink by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Admin Header Badge
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "ADMINISTRATION CONSOLE",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Global stats, deposit monitoring, user bans, and coin grants",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Stats Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatTile(
          label = "Volume",
          value = viewModel.formatCoins(globalStats.totalVolume),
          modifier = Modifier.weight(1f)
        )
        StatTile(
          label = "House Commission",
          value = viewModel.formatCoins(globalStats.totalFeeCollected),
          modifier = Modifier.weight(1f)
        )
      }
    }
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatTile(
          label = "Total Deposits",
          value = viewModel.formatCoins(globalStats.totalDeposits),
          modifier = Modifier.weight(1f)
        )
        StatTile(
          label = "Total Players",
          value = "${nonAdminPlayers.size}",
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Deposit Alerts Section
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "DEPOSIT NOTIFICATIONS (${depositAlerts.size})",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          if (depositAlerts.isEmpty()) {
            Text(
              text = "No deposits received yet.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            depositAlerts.take(10).forEach { alert ->
              val timeStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(alert.timestamp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "${alert.fromUserName} deposited +${viewModel.formatCoins(alert.amount)}",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp,
                  color = WinGreen
                )
                Text(
                  text = timeStr,
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // Grant Coins / VIP Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "GRANT COINS OR VIP PASS",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = grantTargetText,
            onValueChange = { grantTargetText = it },
            label = { Text("Player Name or ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("grant_target_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = grantCoinsText,
              onValueChange = { grantCoinsText = it.filter { c -> c.isDigit() } },
              label = { Text("Coins") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f).testTag("grant_coins_input"),
              shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
              value = grantVipDaysText,
              onValueChange = { grantVipDaysText = it.filter { c -> c.isDigit() } },
              label = { Text("VIP Days") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f).testTag("grant_vip_days_input"),
              shape = RoundedCornerShape(12.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              val c = grantCoinsText.toLongOrNull() ?: 0L
              val v = grantVipDaysText.toIntOrNull() ?: 0
              viewModel.adminGrant(grantTargetText, c, v)
              grantTargetText = ""
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldPrimary,
              contentColor = Color(0xFF221600)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("admin_grant_button")
          ) {
            Text(text = "Grant to Player", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Players Management List
    item {
      Text(
        text = "REGISTERED PLAYERS",
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    items(nonAdminPlayers, key = { it.id }) { player ->
      AdminPlayerItem(
        player = player,
        onToggleBan = { viewModel.adminToggleBan(player.id) },
        formatCoins = { viewModel.formatCoins(it) }
      )
    }

    // Update WhatsApp link
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "OFFICIAL BUY COINS LINK",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = newBuyLinkText,
            onValueChange = { newBuyLinkText = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = { viewModel.adminUpdateWhatsappLink(newBuyLinkText) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.End)
          ) {
            Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Update Link", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Create Giveaway
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "CREATE NEW GIVEAWAY",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = newGiveawayTitle,
            onValueChange = { newGiveawayTitle = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = newGiveawayDesc,
            onValueChange = { newGiveawayDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = newGiveawayLink,
            onValueChange = { newGiveawayLink = it },
            label = { Text("Group URL (https://)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              viewModel.addGiveaway(newGiveawayTitle, newGiveawayDesc, newGiveawayLink)
              newGiveawayTitle = ""
              newGiveawayDesc = ""
              newGiveawayLink = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF221600)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Publish Giveaway", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun StatTile(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = GoldPrimary
      )
    }
  }
}

@Composable
fun AdminPlayerItem(
  player: User,
  onToggleBan: () -> Unit,
  formatCoins: (Long) -> String
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(14.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = player.name,
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          if (player.isBanned) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = Color(0xFFEF4444).copy(alpha = 0.2f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "BANNED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
        }
        Text(
          text = "ID #${player.id} • ${formatCoins(player.balance)} coins • ${player.tier.title}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }

      OutlinedButton(
        onClick = onToggleBan,
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = if (player.isBanned) WinGreen else Color(0xFFEF4444)
        ),
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(
          imageVector = if (player.isBanned) Icons.Default.Check else Icons.Default.Block,
          contentDescription = null,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (player.isBanned) "Unban" else "Ban",
          fontSize = 11.sp,
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
