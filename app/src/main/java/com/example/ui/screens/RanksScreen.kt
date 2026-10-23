package com.example.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TierLevel
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.VipPurple
import com.example.ui.theme.WinGreen
import com.example.viewmodel.CoinFlipViewModel

@Composable
fun RanksScreen(
  viewModel: CoinFlipViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val users by viewModel.users.collectAsState()
  val history by viewModel.gameHistory.collectAsState()
  val currentPeriod by viewModel.leaderboardPeriod.collectAsState()

  val myTier = currentUser?.tier ?: TierLevel.BRONZE
  val nextTier = TierLevel.nextTier(myTier)
  val myWager = currentUser?.wagered ?: 0L

  val progressToNext = if (nextTier != null) {
    val range = (nextTier.minWager - myTier.minWager).toFloat()
    val curr = (myWager - myTier.minWager).toFloat()
    (curr / range).coerceIn(0f, 1f)
  } else 1f

  // Calculate filtered stats based on period
  val periodCutoff = remember(currentPeriod) {
    val now = System.currentTimeMillis()
    when (currentPeriod) {
      "24h" -> now - 24L * 60 * 60 * 1000L
      "7d" -> now - 7L * 24 * 60 * 60 * 1000L
      else -> 0L
    }
  }

  val filteredHistory = remember(history, periodCutoff) {
    history.filter { it.timestamp >= periodCutoff }
  }

  // Derive Top Winners, Highest Earners, and Streaks
  val statsMap = remember(filteredHistory, users) {
    val map = mutableMapOf<Long, PlayerAggregates>()
    users.filter { it.role != "admin" }.forEach { u ->
      map[u.id] = PlayerAggregates(u.id, u.name, u.tier.title)
    }

    filteredHistory.forEach { item ->
      val w = map.getOrPut(item.winnerId) { PlayerAggregates(item.winnerId, item.winnerName, "Bronze") }
      val l = map.getOrPut(item.loserId) { PlayerAggregates(item.loserId, item.loserName, "Bronze") }
      w.wins++
      w.netEarned += (item.payout - item.stake)
      w.currentStreak++
      if (w.currentStreak > w.bestStreak) w.bestStreak = w.currentStreak
      l.currentStreak = 0
      l.netEarned -= item.stake
    }
    map.values.toList()
  }

  val topWinners = remember(statsMap) {
    statsMap.filter { it.wins > 0 }.sortedByDescending { it.wins }.take(5)
  }
  val topEarners = remember(statsMap) {
    statsMap.filter { it.netEarned > 0 }.sortedByDescending { it.netEarned }.take(5)
  }
  val topStreaks = remember(statsMap) {
    statsMap.filter { it.bestStreak > 0 }.sortedByDescending { it.bestStreak }.take(5)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Current Tier Card
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
            Column {
              Text(
                text = "YOUR LEVEL",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${myTier.title} Rank",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = GoldPrimary
              )
            }

            Surface(
              color = GoldPrimary.copy(alpha = 0.18f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.WorkspacePremium,
                  contentDescription = null,
                  tint = GoldPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${(currentUser?.feeBps ?: 1000L) / 100.0}% Fee",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = GoldPrimary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Wagered stat
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Total Wagered: ${viewModel.formatCoins(myWager)} coins",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )

            if (nextTier != null) {
              val remaining = (nextTier.minWager - myWager).coerceAtLeast(0L)
              Text(
                text = "${viewModel.formatCoins(remaining)} to ${nextTier.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            } else {
              Text(
                text = "Max Level Reached!",
                style = MaterialTheme.typography.bodySmall,
                color = WinGreen
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          LinearProgressIndicator(
            progress = { progressToNext },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = GoldPrimary,
            trackColor = MaterialTheme.colorScheme.surface
          )

          if (currentUser?.isVip == true) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              color = VipPurple.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "👑 VIP Perk Applied: Extra 2.0% fee reduction active",
                style = MaterialTheme.typography.bodySmall,
                color = VipPurple,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // Time Period Filter Tabs
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val periods = listOf("24h" to "24 Hours", "7d" to "7 Days", "all" to "All Time")
        periods.forEach { (code, label) ->
          val isSelected = currentPeriod == code
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) GoldPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
              .border(
                width = 1.dp,
                color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
              )
              .clickable { viewModel.setLeaderboardPeriod(code) }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
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

    // Top Winners Board
    item {
      LeaderboardSectionCard(
        title = "Top Winners (Matches Won)",
        icon = Icons.Default.EmojiEvents,
        items = topWinners.mapIndexed { idx, it ->
          LeaderboardRowData(
            rank = idx + 1,
            name = it.name,
            tier = it.tier,
            score = "${it.wins} wins"
          )
        }
      )
    }

    // Highest Earners Board
    item {
      LeaderboardSectionCard(
        title = "Highest Net Earners (Coins)",
        icon = Icons.Default.TrendingUp,
        items = topEarners.mapIndexed { idx, it ->
          LeaderboardRowData(
            rank = idx + 1,
            name = it.name,
            tier = it.tier,
            score = "+${viewModel.formatCoins(it.netEarned)}"
          )
        }
      )
    }

    // Longest Win Streaks Board
    item {
      LeaderboardSectionCard(
        title = "Longest Win Streaks",
        icon = Icons.Default.LocalFireDepartment,
        items = topStreaks.mapIndexed { idx, it ->
          LeaderboardRowData(
            rank = idx + 1,
            name = it.name,
            tier = it.tier,
            score = "${it.bestStreak} streak"
          )
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

private class PlayerAggregates(
  val id: Long,
  val name: String,
  val tier: String,
  var wins: Int = 0,
  var netEarned: Long = 0L,
  var currentStreak: Int = 0,
  var bestStreak: Int = 0
)

data class LeaderboardRowData(
  val rank: Int,
  val name: String,
  val tier: String,
  val score: String
)

@Composable
fun LeaderboardSectionCard(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  items: List<LeaderboardRowData>
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = title,
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (items.isEmpty()) {
        Text(
          text = "No recorded matches in this period.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(vertical = 8.dp)
        )
      } else {
        items.forEach { row ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              // Medal / Rank Number
              val badgeColor = when (row.rank) {
                1 -> GoldPrimary
                2 -> SilverPrimary
                3 -> Color(0xFFCD7F32)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
              }

              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${row.rank}",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = badgeColor
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Text(
                  text = row.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${row.tier} Tier",
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Text(
              text = row.score,
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}
