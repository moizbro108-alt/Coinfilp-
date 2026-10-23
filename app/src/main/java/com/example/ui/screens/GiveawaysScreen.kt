package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Giveaway
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.VipPurple
import com.example.ui.theme.WinGreen
import com.example.viewmodel.CoinFlipViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GiveawaysScreen(
  viewModel: CoinFlipViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentUser by viewModel.currentUser.collectAsState()
  val giveaways by viewModel.giveaways.collectAsState()
  val isVip = currentUser?.isVip == true
  val vipUntil = currentUser?.vipUntil ?: 0L

  val vipExpiryStr = remember(vipUntil) {
    if (vipUntil > System.currentTimeMillis()) {
      SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(vipUntil))
    } else null
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // VIP Pass Banner Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = VipPurple,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "VIP MEMBERSHIP PASS",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              color = VipPurple.copy(alpha = 0.18f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = if (isVip) "ACTIVE" else "10,000 COINS",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                color = VipPurple,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "VIP members receive an exclusive 2.0% commission discount on every coin flip match, custom profile styling, and access to monthly coin airdrops.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (isVip && vipExpiryStr != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "✓ VIP Pass Active until $vipExpiryStr",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = WinGreen
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { viewModel.buyVip() },
            colors = ButtonDefaults.buttonColors(
              containerColor = VipPurple,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("buy_vip_button")
          ) {
            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isVip) "Extend VIP (30 Days)" else "Get VIP (10,000 Coins / 30 Days)",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    }

    // Community Giveaways Section Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "COMMUNITY GIVEAWAYS",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onBackground
        )

        Icon(
          imageVector = Icons.Default.CardGiftcard,
          contentDescription = null,
          tint = GoldPrimary
        )
      }
    }

    if (giveaways.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text(
            text = "No giveaways active at this moment. Check back soon for weekend raffles!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(giveaways, key = { it.id }) { giveaway ->
        GiveawayItemCard(
          giveaway = giveaway,
          onOpenLink = {
            runCatching {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(giveaway.linkUrl))
              context.startActivity(intent)
            }
          }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun GiveawayItemCard(
  giveaway: Giveaway,
  onOpenLink: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp),
    border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = giveaway.title,
        fontFamily = ChakraPetchFont,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = giveaway.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = onOpenLink,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.align(Alignment.End).testTag("join_giveaway_${giveaway.id}")
      ) {
        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Join Giveaway",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }
    }
  }
}
