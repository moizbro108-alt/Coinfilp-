package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoinSide
import com.example.model.GameOffer
import com.example.ui.components.Coin3DView
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SilverDark
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.WinGreen
import com.example.viewmodel.CoinFlipViewModel

@Composable
fun PlayScreen(
  viewModel: CoinFlipViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val selectedSide by viewModel.selectedSide.collectAsState()
  val selectedStake by viewModel.selectedStake.collectAsState()
  val isFlipping by viewModel.isFlipping.collectAsState()
  val coinRotationY by viewModel.coinRotationY.collectAsState()
  val coinLiftFraction by viewModel.coinLiftFraction.collectAsState()
  val gameStatusMessage by viewModel.gameStatusMessage.collectAsState()
  val lastOutcome by viewModel.lastGameOutcome.collectAsState()
  val openGames by viewModel.openGames.collectAsState()

  var stakeInputText by remember(selectedStake) { mutableStateOf(selectedStake.toString()) }

  val userBps = currentUser?.feeBps ?: 1000L
  val settlement = viewModel.repository.calculateSettlement(selectedStake, userBps)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top 3D Coin Stage
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
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Status label / Win callout
          Text(
            text = gameStatusMessage,
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = when {
              lastOutcome?.isUserWinner == true -> WinGreen
              lastOutcome != null && !lastOutcome!!.isUserWinner -> Color(0xFFEF4444)
              else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(bottom = 6.dp)
          )

          // 3D Coin Canvas
          Coin3DView(
            rotationY = coinRotationY,
            liftFraction = coinLiftFraction,
            isFlipping = isFlipping,
            onClick = {
              if (!isFlipping) viewModel.playSoloPracticeFlip()
            }
          )

          Text(
            text = if (isFlipping) "Flipping in the air..." else "Tap coin to test flip • Select side below",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }

    // Side Selection & Stake Configuration Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "PICK YOUR SIDE",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Segmented Side Selector (Heads / Tails)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Heads button
            val headsSelected = selectedSide == CoinSide.HEADS
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (headsSelected) GoldPrimary.copy(alpha = 0.18f) else Color.Transparent
                )
                .border(
                  width = if (headsSelected) 2.dp else 1.dp,
                  color = if (headsSelected) GoldPrimary else MaterialTheme.colorScheme.outline,
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { viewModel.selectSide(CoinSide.HEADS) }
                .padding(vertical = 12.dp)
                .testTag("select_heads_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "HEADS",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  color = if (headsSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface,
                  fontSize = 15.sp
                )
              }
            }

            // Tails button
            val tailsSelected = selectedSide == CoinSide.TAILS
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (tailsSelected) SilverPrimary.copy(alpha = 0.2f) else Color.Transparent
                )
                .border(
                  width = if (tailsSelected) 2.dp else 1.dp,
                  color = if (tailsSelected) SilverPrimary else MaterialTheme.colorScheme.outline,
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { viewModel.selectSide(CoinSide.TAILS) }
                .padding(vertical = 12.dp)
                .testTag("select_tails_button"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SilverPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "TAILS",
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  color = if (tailsSelected) SilverPrimary else MaterialTheme.colorScheme.onSurface,
                  fontSize = 15.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Stake Amount Input
          Text(
            text = "STAKE (COINS)",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(6.dp))

          OutlinedTextField(
            value = stakeInputText,
            onValueChange = { input ->
              stakeInputText = input.filter { it.isDigit() }
              val parsed = stakeInputText.toLongOrNull() ?: 100L
              viewModel.setStake(parsed)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldPrimary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline,
              focusedTextColor = MaterialTheme.colorScheme.onSurface,
              unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("stake_input_field"),
            leadingIcon = {
              Text(
                text = "🪙",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 12.dp)
              )
            }
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Quick Chip Selection Buttons (100, 1K, 10K, 100K, 1M, MAX)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val chips = listOf(
              "100" to 100L,
              "500" to 500L,
              "1K" to 1000L,
              "10K" to 10000L,
              "50K" to 50000L,
              "MAX" to (currentUser?.balance ?: 0L).coerceAtLeast(100L)
            )

            chips.forEach { (label, amount) ->
              val isCurrent = selectedStake == amount
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isCurrent) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                  )
                  .border(
                    width = 1.dp,
                    color = if (isCurrent) GoldPrimary else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable { viewModel.setStake(amount) }
                  .padding(vertical = 7.dp)
                  .testTag("chip_button_$label"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  fontFamily = ChakraPetchFont,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = if (isCurrent) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(10.dp))

          // Pot & Commission Calculation
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Total Match Pot:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${viewModel.formatCoins(settlement.pot)} coins",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              val feePct = (userBps / 100.0)
              Text(
                text = "Commission ($feePct%):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "-${viewModel.formatCoins(settlement.fee)} coins",
                fontFamily = ChakraPetchFont,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Winner Receives:",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                color = WinGreen,
                fontSize = 14.sp
              )
              Text(
                text = "+${viewModel.formatCoins(settlement.payout)} coins",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                color = WinGreen,
                fontSize = 15.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Action Buttons: Create Game + Solo Flip
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { viewModel.createGame() },
              enabled = !isFlipping && (currentUser?.balance ?: 0L) >= selectedStake,
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color(0xFF221600)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1.3f)
                .height(48.dp)
                .testTag("create_game_button")
            ) {
              Icon(imageVector = Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Create Game",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }

            OutlinedButton(
              onClick = { viewModel.playSoloPracticeFlip() },
              enabled = !isFlipping,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("practice_flip_button")
            ) {
              Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Practice",
                fontFamily = ChakraPetchFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // Open Games Lobby Section
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "OPEN GAMES LOBBY",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onBackground
        )

        Text(
          text = "${openGames.size} live challenges",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (openGames.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "🎲",
              fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "No open matches right now",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Create a game above and other players will join you!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(openGames, key = { it.id }) { game ->
        LobbyGameItem(
          game = game,
          currentUserId = currentUser?.id,
          onJoin = { viewModel.joinGame(game) },
          onCancel = { viewModel.cancelGame(game) },
          isFlipping = isFlipping
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun LobbyGameItem(
  game: GameOffer,
  currentUserId: Long?,
  onJoin: () -> Unit,
  onCancel: () -> Unit,
  isFlipping: Boolean,
  modifier: Modifier = Modifier
) {
  val isMine = game.byUserId == currentUserId
  val oppositeSide = game.side.opposite()

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    shape = RoundedCornerShape(14.dp),
    border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Dot indicator
        Box(
          modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (game.side == CoinSide.HEADS) GoldPrimary else SilverPrimary)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = game.byUserName,
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            if (isMine) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = GoldPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "YOU",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = GoldPrimary,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }
          Text(
            text = "Picked ${game.side.displayName} • Stake: ${String.format("%,d", game.stake)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Action button
      if (isMine) {
        OutlinedButton(
          onClick = onCancel,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("cancel_game_${game.id}")
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Cancel", fontSize = 12.sp, fontFamily = ChakraPetchFont)
        }
      } else {
        Button(
          onClick = onJoin,
          enabled = !isFlipping,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (oppositeSide == CoinSide.HEADS) GoldPrimary else SilverPrimary,
            contentColor = if (oppositeSide == CoinSide.HEADS) Color(0xFF221600) else Color(0xFF14243B)
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("join_game_${game.id}")
        ) {
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Join ${oppositeSide.displayName}",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}
