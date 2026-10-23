package com.example.model

enum class CoinSide(val displayName: String) {
  HEADS("Heads"),
  TAILS("Tails");

  fun opposite(): CoinSide = if (this == HEADS) TAILS else HEADS
}

enum class TierLevel(val title: String, val minWager: Long, val baseFeeBps: Long) {
  BRONZE("Bronze", 0L, 1000L),       // 10.0%
  SILVER("Silver", 10_000L, 900L),    // 9.0%
  GOLD("Gold", 100_000L, 800L),       // 8.0%
  PLATINUM("Platinum", 1_000_000L, 700L), // 7.0%
  DIAMOND("Diamond", 10_000_000L, 600L); // 6.0%

  companion object {
    fun fromWager(wager: Long, forcedVipTier: Int? = null): TierLevel {
      if (forcedVipTier != null && forcedVipTier in 0..4) {
        return entries[forcedVipTier]
      }
      return entries.lastOrNull { wager >= it.minWager } ?: BRONZE
    }

    fun nextTier(current: TierLevel): TierLevel? {
      val idx = entries.indexOf(current)
      return if (idx < entries.size - 1) entries[idx + 1] else null
    }
  }
}

data class User(
  val id: Long,
  val name: String,
  val pass: String,
  val role: String = "player", // "player" or "admin"
  val balance: Long = 0L,
  val wagered: Long = 0L,
  val deposited: Long = 0L,
  val vipUntil: Long? = null,
  val forcedTierIndex: Int? = null,
  val isBanned: Boolean = false,
  val friendIds: List<Long> = emptyList()
) {
  val isVip: Boolean
    get() = (vipUntil ?: 0L) > System.currentTimeMillis()

  val tier: TierLevel
    get() = TierLevel.fromWager(wagered, forcedTierIndex)

  // Commission fee in basis points (100 bps = 1%). VIP membership drops fee by 200 bps (2%)
  val feeBps: Long
    get() = (tier.baseFeeBps - (if (isVip) 200L else 0L)).coerceAtLeast(300L)
}

data class GameOffer(
  val id: Long,
  val byUserId: Long,
  val byUserName: String,
  val side: CoinSide,
  val stake: Long,
  val invitedUserId: Long? = null
)

data class Settlement(
  val pot: Long,
  val fee: Long,
  val payout: Long
)

data class GameHistoryItem(
  val id: Long,
  val timestamp: Long,
  val winnerId: Long,
  val winnerName: String,
  val loserId: Long,
  val loserName: String,
  val stake: Long,
  val payout: Long,
  val resultSide: CoinSide
)

data class TransactionRecord(
  val id: Long,
  val timestamp: Long,
  val description: String,
  val amountChange: Long = 0L
)

data class ChatMessage(
  val id: Long,
  val fromUserId: Long,
  val toUserId: Long,
  val message: String,
  val timestamp: Long
)

data class Giveaway(
  val id: Long,
  val title: String,
  val description: String,
  val linkUrl: String
)

enum class NotificationType {
  GAME_CHALLENGE,
  GAME_WIN,
  VIP_PERK,
  COMMUNITY,
  SYSTEM
}

data class AppNotification(
  val id: String,
  val title: String,
  val message: String,
  val timestamp: Long,
  val type: NotificationType,
  val isRead: Boolean = false,
  val actionData: String? = null
)

data class DepositAlert(
  val id: Long,
  val timestamp: Long,
  val fromUserId: Long,
  val fromUserName: String,
  val amount: Long,
  val isRead: Boolean = false
)

enum class AppNavTab(val label: String) {
  PLAY("Play"),
  WALLET("Wallet"),
  RANKS("Ranks"),
  FRIENDS("Friends"),
  GIVEAWAYS("Giveaway"),
  ADMIN("Admin")
}

enum class ThemeMode {
  SYSTEM,
  DARK,
  LIGHT
}

data class GlobalStats(
  val totalVolume: Long = 0L,
  val totalFeeCollected: Long = 0L,
  val totalDeposits: Long = 0L
)
