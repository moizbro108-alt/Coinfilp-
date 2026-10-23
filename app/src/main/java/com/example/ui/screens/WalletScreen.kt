package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionRecord
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.VipPurple
import com.example.ui.theme.WinGreen
import com.example.viewmodel.CoinFlipViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
  viewModel: CoinFlipViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentUser by viewModel.currentUser.collectAsState()
  val whatsappLink by viewModel.whatsappBuyLink.collectAsState()

  var depositAmountText by remember { mutableStateOf("1000") }
  var giftRecipientText by remember { mutableStateOf("") }
  var giftAmountText by remember { mutableStateOf("500") }

  val transactions = remember(currentUser?.id) {
    currentUser?.let { viewModel.repository.getTransactions(it.id) } ?: emptyList()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Hero Balance Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "TOTAL BALANCE",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "🪙",
                  fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = viewModel.formatCoins(currentUser?.balance ?: 0L),
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 32.sp,
                  color = GoldPrimary
                )
              }
            }

            // Tier & VIP Badges
            Column(horizontalAlignment = Alignment.End) {
              Surface(
                color = GoldPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
              ) {
                Text(
                  text = "${currentUser?.tier?.title?.uppercase()} TIER",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }

              if (currentUser?.isVip == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                  color = VipPurple.copy(alpha = 0.18f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = "👑 VIP ACTIVE",
                    fontFamily = ChakraPetchFont,
                    fontWeight = FontWeight.Bold,
                    color = VipPurple,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }

        }
      }
    }

    // Deposit Coins & Buy Coins
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "DEPOSIT OR BUY COINS",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Depositing transfers coins to the admin treasury and alerts admin.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
          )

          OutlinedTextField(
            value = depositAmountText,
            onValueChange = { depositAmountText = it.filter { c -> c.isDigit() } },
            label = { Text("Deposit Amount (coins)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("deposit_amount_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                val amt = depositAmountText.toLongOrNull() ?: 0L
                viewModel.depositCoins(amt)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color(0xFF221600)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).height(46.dp).testTag("deposit_button")
            ) {
              Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = "Deposit", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappLink))
                context.startActivity(intent)
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).height(46.dp).testTag("buy_coins_whatsapp_button")
            ) {
              Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = "Buy via Chat", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Gift Coins Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "GIFT COINS TO A PLAYER",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = giftRecipientText,
            onValueChange = { giftRecipientText = it },
            label = { Text("Player Name or ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("gift_recipient_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = giftAmountText,
            onValueChange = { giftAmountText = it.filter { c -> c.isDigit() } },
            label = { Text("Coins Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("gift_amount_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              val amt = giftAmountText.toLongOrNull() ?: 0L
              viewModel.giftCoins(giftRecipientText, amt)
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("send_gift_button")
          ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Send Gift", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Recent Activity Ledger
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RECENT ACTIVITY",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onBackground
        )

        Icon(
          imageVector = Icons.Default.History,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (transactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text(
            text = "No recent transactions yet. Play a game or claim your daily bonus!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(transactions, key = { it.id }) { record ->
        TransactionRowItem(record = record)
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun TransactionRowItem(record: TransactionRecord) {
  val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
  val formattedDate = remember(record.timestamp) { dateFormat.format(Date(record.timestamp)) }
  val isPositive = record.amountChange > 0
  val isNegative = record.amountChange < 0

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = record.description,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = formattedDate,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }

      if (record.amountChange != 0L) {
        Text(
          text = (if (isPositive) "+" else "") + String.format("%,d", record.amountChange),
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = if (isPositive) WinGreen else if (isNegative) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}
