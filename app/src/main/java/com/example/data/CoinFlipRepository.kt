package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppNotification
import com.example.model.ChatMessage
import com.example.model.CoinSide
import com.example.model.DepositAlert
import com.example.model.GameHistoryItem
import com.example.model.GameOffer
import com.example.model.Giveaway
import com.example.model.GlobalStats
import com.example.model.NotificationType
import com.example.model.Settlement
import com.example.model.ThemeMode
import com.example.model.TransactionRecord
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import kotlin.math.floor
import kotlin.math.min

class CoinFlipRepository(private val context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("coin_flip_data_store_v2", Context.MODE_PRIVATE)

  private val random = SecureRandom()

  // State flows
  private val _users = MutableStateFlow<List<User>>(emptyList())
  val users: StateFlow<List<User>> = _users.asStateFlow()

  private val _currentUserId = MutableStateFlow<Long?>(null)
  val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

  private val _openGames = MutableStateFlow<List<GameOffer>>(emptyList())
  val openGames: StateFlow<List<GameOffer>> = _openGames.asStateFlow()

  private val _gameHistory = MutableStateFlow<List<GameHistoryItem>>(emptyList())
  val gameHistory: StateFlow<List<GameHistoryItem>> = _gameHistory.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val _giveaways = MutableStateFlow<List<Giveaway>>(emptyList())
  val giveaways: StateFlow<List<Giveaway>> = _giveaways.asStateFlow()

  private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
  val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

  private val _depositAlerts = MutableStateFlow<List<DepositAlert>>(emptyList())
  val depositAlerts: StateFlow<List<DepositAlert>> = _depositAlerts.asStateFlow()

  private val _globalStats = MutableStateFlow(GlobalStats())
  val globalStats: StateFlow<GlobalStats> = _globalStats.asStateFlow()

  private val _whatsappBuyLink = MutableStateFlow("https://chat.whatsapp.com/your-buy-coins-group")
  val whatsappBuyLink: StateFlow<String> = _whatsappBuyLink.asStateFlow()

  private val _themeMode = MutableStateFlow(ThemeMode.DARK)
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  private val _isCloudSyncing = MutableStateFlow(false)
  val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

  private val _lastSyncedTimestamp = MutableStateFlow(System.currentTimeMillis())
  val lastSyncedTimestamp: StateFlow<Long> = _lastSyncedTimestamp.asStateFlow()

  private val _hapticsEnabled = MutableStateFlow(true)
  val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

  private var nextGameId = 100L
  private var nextUserId = 30L
  private var nextMessageId = 50L

  init {
    loadFromDisk()
  }

  fun getCurrentUser(): User? {
    val uid = _currentUserId.value ?: return null
    return _users.value.find { it.id == uid }
  }

  // --- Auth & Session ---
  fun login(username: String, pass: String): Result<User> {
    val trimmed = username.trim()
    val user = _users.value.find { it.name.equals(trimmed, ignoreCase = true) }
      ?: return Result.failure(IllegalArgumentException("User '$trimmed' not found"))

    if (user.pass != pass) {
      return Result.failure(IllegalArgumentException("Incorrect password"))
    }
    if (user.isBanned) {
      return Result.failure(IllegalStateException("This account is currently suspended"))
    }

    _currentUserId.value = user.id
    saveToDisk()
    return Result.success(user)
  }

  fun signup(username: String, pass: String): Result<User> {
    val trimmed = username.trim()
    if (!trimmed.matches(Regex("^[a-zA-Z0-9_]{3,16}$"))) {
      return Result.failure(IllegalArgumentException("Username must be 3-16 letters, numbers, or _"))
    }
    if (pass.length < 4) {
      return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))
    }
    if (_users.value.any { it.name.equals(trimmed, ignoreCase = true) }) {
      return Result.failure(IllegalArgumentException("Username '$trimmed' is already taken"))
    }

    val newUser = User(
      id = nextUserId++,
      name = trimmed,
      pass = pass,
      role = "player",
      balance = 0L,
      wagered = 0L,
      deposited = 0L
    )

    _users.value = _users.value + newUser
    _currentUserId.value = newUser.id
    pushNotification(
      AppNotification(
        id = "welcome_${System.currentTimeMillis()}",
        title = "Welcome to Coin Flip!",
        message = "Account created successfully.",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.SYSTEM
      )
    )
    saveToDisk()
    return Result.success(newUser)
  }

  fun logout() {
    _currentUserId.value = null
    saveToDisk()
  }

  // --- Gameplay Engine ---
  fun calculateSettlement(stake: Long, feeBps: Long): Settlement {
    val pot = stake * 2
    val fee = floor(pot * feeBps / 10000.0).toLong()
    val payout = pot - fee
    return Settlement(pot, fee, payout)
  }

  fun createGame(byUserId: Long, side: CoinSide, stake: Long, invitedUserId: Long? = null): Result<GameOffer> {
    val user = _users.value.find { it.id == byUserId }
      ?: return Result.failure(IllegalArgumentException("User not found"))

    if (stake < 100L) {
      return Result.failure(IllegalArgumentException("Minimum stake is 100 coins"))
    }
    if (stake > user.balance) {
      return Result.failure(IllegalArgumentException("Insufficient coin balance"))
    }

    // Deduct stake from creator
    updateUser(user.copy(balance = user.balance - stake))
    addTransaction(user.id, "Staked ${formatCoins(stake)} on ${side.displayName}", -stake)

    val game = GameOffer(
      id = nextGameId++,
      byUserId = user.id,
      byUserName = user.name,
      side = side,
      stake = stake,
      invitedUserId = invitedUserId
    )

    _openGames.value = listOf(game) + _openGames.value
    saveToDisk()
    return Result.success(game)
  }

  fun cancelGame(gameId: Long, byUserId: Long): Result<Unit> {
    val game = _openGames.value.find { it.id == gameId }
      ?: return Result.failure(IllegalArgumentException("Game offer no longer exists"))

    if (game.byUserId != byUserId) {
      return Result.failure(IllegalStateException("Only the creator can cancel this game"))
    }

    val user = _users.value.find { it.id == byUserId }
    if (user != null) {
      updateUser(user.copy(balance = user.balance + game.stake))
      addTransaction(user.id, "Refund for cancelled game: +${formatCoins(game.stake)}", game.stake)
    }

    _openGames.value = _openGames.value.filter { it.id != gameId }
    saveToDisk()
    return Result.success(Unit)
  }

  data class PlayOutcome(
    val resultSide: CoinSide,
    val winner: User,
    val loser: User,
    val settlement: Settlement,
    val isUserWinner: Boolean
  )

  fun executeGame(gameId: Long, joiningUserId: Long): Result<PlayOutcome> {
    val game = _openGames.value.find { it.id == gameId }
      ?: return Result.failure(IllegalArgumentException("Game no longer available in lobby"))

    val joiningUser = _users.value.find { it.id == joiningUserId }
      ?: return Result.failure(IllegalArgumentException("Player not found"))

    if (joiningUser.balance < game.stake) {
      return Result.failure(IllegalArgumentException("Not enough coins to join this stake"))
    }

    val creator = _users.value.find { it.id == game.byUserId }
      ?: return Result.failure(IllegalArgumentException("Game creator not found"))

    // Deduct stake from joining player
    val updatedJoiner = joiningUser.copy(
      balance = joiningUser.balance - game.stake,
      wagered = joiningUser.wagered + game.stake
    )
    val updatedCreator = creator.copy(
      wagered = creator.wagered + game.stake
    )

    // Flip the coin (50/50 fair cryptographic random)
    val randomByte = ByteArray(1).also { random.nextBytes(it) }[0]
    val resultSide = if ((randomByte.toInt() and 1) == 0) CoinSide.HEADS else CoinSide.TAILS

    // Game creator picked game.side. Joining player gets opposite.
    val creatorWon = resultSide == game.side
    val minBps = min(updatedCreator.feeBps, updatedJoiner.feeBps)
    val settlement = calculateSettlement(game.stake, minBps)

    val winnerAfterPay: User
    val loserAfterPay: User

    if (creatorWon) {
      winnerAfterPay = updatedCreator.copy(balance = updatedCreator.balance + settlement.payout)
      loserAfterPay = updatedJoiner
    } else {
      winnerAfterPay = updatedJoiner.copy(balance = updatedJoiner.balance + settlement.payout)
      loserAfterPay = updatedCreator
    }

    updateUser(winnerAfterPay)
    updateUser(loserAfterPay)

    // Log history and stats
    addTransaction(
      winnerAfterPay.id,
      "Won +${formatCoins(settlement.payout)} vs ${loserAfterPay.name} (${resultSide.displayName})",
      settlement.payout - game.stake
    )
    addTransaction(
      loserAfterPay.id,
      "Lost -${formatCoins(game.stake)} vs ${winnerAfterPay.name} (${resultSide.displayName})",
      -game.stake
    )

    val history = GameHistoryItem(
      id = System.currentTimeMillis(),
      timestamp = System.currentTimeMillis(),
      winnerId = winnerAfterPay.id,
      winnerName = winnerAfterPay.name,
      loserId = loserAfterPay.id,
      loserName = loserAfterPay.name,
      stake = game.stake,
      payout = settlement.payout,
      resultSide = resultSide
    )
    _gameHistory.value = (listOf(history) + _gameHistory.value).take(100)

    _globalStats.value = _globalStats.value.copy(
      totalVolume = _globalStats.value.totalVolume + settlement.pot,
      totalFeeCollected = _globalStats.value.totalFeeCollected + settlement.fee
    )

    _openGames.value = _openGames.value.filter { it.id != gameId }
    saveToDisk()

    return Result.success(
      PlayOutcome(
        resultSide = resultSide,
        winner = winnerAfterPay,
        loser = loserAfterPay,
        settlement = settlement,
        isUserWinner = winnerAfterPay.id == joiningUserId
      )
    )
  }

  // --- Wallet Operations ---
  fun depositCoins(fromUserId: Long, amount: Long): Result<Unit> {
    val user = _users.value.find { it.id == fromUserId }
      ?: return Result.failure(IllegalArgumentException("User not found"))

    if (amount < 100L) {
      return Result.failure(IllegalArgumentException("Minimum deposit is 100 coins"))
    }
    if (amount > user.balance) {
      return Result.failure(IllegalArgumentException("Insufficient coin balance to deposit"))
    }

    val admin = _users.value.find { it.role == "admin" }
      ?: return Result.failure(IllegalStateException("Admin account not found"))

    updateUser(user.copy(balance = user.balance - amount, deposited = user.deposited + amount))
    updateUser(admin.copy(balance = admin.balance + amount))

    addTransaction(user.id, "Deposit to Admin -$amount", -amount)
    addTransaction(admin.id, "Deposit received from ${user.name} +$amount", amount)

    val alert = DepositAlert(
      id = System.currentTimeMillis(),
      timestamp = System.currentTimeMillis(),
      fromUserId = user.id,
      fromUserName = user.name,
      amount = amount
    )
    _depositAlerts.value = listOf(alert) + _depositAlerts.value
    _globalStats.value = _globalStats.value.copy(
      totalDeposits = _globalStats.value.totalDeposits + amount
    )

    saveToDisk()
    return Result.success(Unit)
  }

  fun giftCoins(fromUserId: Long, targetIdentifier: String, amount: Long): Result<User> {
    val fromUser = _users.value.find { it.id == fromUserId }
      ?: return Result.failure(IllegalArgumentException("Sender user not found"))

    if (amount <= 0 || amount > fromUser.balance) {
      return Result.failure(IllegalArgumentException("Invalid transfer amount"))
    }

    val trimmed = targetIdentifier.trim()
    val targetUser = _users.value.find {
      it.id.toString() == trimmed || it.name.equals(trimmed, ignoreCase = true)
    } ?: return Result.failure(IllegalArgumentException("Player '$trimmed' not found"))

    if (targetUser.id == fromUser.id) {
      return Result.failure(IllegalArgumentException("Cannot send coins to yourself"))
    }

    updateUser(fromUser.copy(balance = fromUser.balance - amount))
    updateUser(targetUser.copy(balance = targetUser.balance + amount))

    addTransaction(fromUser.id, "Gifted to ${targetUser.name}: -$amount", -amount)
    addTransaction(targetUser.id, "Gift received from ${fromUser.name}: +$amount", amount)

    saveToDisk()
    return Result.success(targetUser)
  }

  // --- VIP & Perks ---
  fun buyVip(userId: Long): Result<Unit> {
    val user = _users.value.find { it.id == userId }
      ?: return Result.failure(IllegalArgumentException("User not found"))

    val vipCost = 10_000L
    if (user.balance < vipCost) {
      return Result.failure(IllegalArgumentException("10,000 coins required to purchase 30-Day VIP"))
    }

    val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
    val currentVip = user.vipUntil ?: 0L
    val baseTime = if (currentVip > System.currentTimeMillis()) currentVip else System.currentTimeMillis()
    val newVipUntil = baseTime + thirtyDaysMs

    updateUser(user.copy(balance = user.balance - vipCost, vipUntil = newVipUntil))
    addTransaction(user.id, "30-Day VIP Pass: -$vipCost", -vipCost)

    pushNotification(
      AppNotification(
        id = "vip_${System.currentTimeMillis()}",
        title = "VIP Status Active! 👑",
        message = "You enjoy 2% lower commission on all coin flip matches for 30 days.",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.VIP_PERK
      )
    )

    saveToDisk()
    return Result.success(Unit)
  }

  // --- Friends & Messaging ---
  fun addFriend(userId: Long, targetIdentifier: String): Result<User> {
    val user = _users.value.find { it.id == userId }
      ?: return Result.failure(IllegalArgumentException("User not found"))

    val trimmed = targetIdentifier.trim()
    val friend = _users.value.find {
      it.id.toString() == trimmed || it.name.equals(trimmed, ignoreCase = true)
    } ?: return Result.failure(IllegalArgumentException("Player '$trimmed' not found"))

    if (friend.id == user.id) {
      return Result.failure(IllegalArgumentException("Cannot add yourself as a friend"))
    }
    if (user.friendIds.contains(friend.id)) {
      return Result.failure(IllegalArgumentException("Player is already in your friends list"))
    }

    updateUser(user.copy(friendIds = user.friendIds + friend.id))
    saveToDisk()
    return Result.success(friend)
  }

  fun removeFriend(userId: Long, friendId: Long) {
    val user = _users.value.find { it.id == userId } ?: return
    updateUser(user.copy(friendIds = user.friendIds.filter { it != friendId }))
    saveToDisk()
  }

  fun sendChatMessage(fromUserId: Long, toUserId: Long, text: String): Result<ChatMessage> {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) {
      return Result.failure(IllegalArgumentException("Message cannot be empty"))
    }

    val message = ChatMessage(
      id = nextMessageId++,
      fromUserId = fromUserId,
      toUserId = toUserId,
      message = trimmed.take(300),
      timestamp = System.currentTimeMillis()
    )

    _chatMessages.value = _chatMessages.value + message
    saveToDisk()
    return Result.success(message)
  }

  fun getChatConversation(userId1: Long, userId2: Long): List<ChatMessage> {
    return _chatMessages.value.filter {
      (it.fromUserId == userId1 && it.toUserId == userId2) ||
        (it.fromUserId == userId2 && it.toUserId == userId1)
    }.sortedBy { it.timestamp }
  }

  // --- Notifications ---
  fun pushNotification(notification: AppNotification) {
    _notifications.value = listOf(notification) + _notifications.value
    saveToDisk()
  }

  fun markNotificationsAsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    saveToDisk()
  }

  fun clearNotifications() {
    _notifications.value = emptyList()
    saveToDisk()
  }

  // --- Admin Controls ---
  fun setWhatsappLink(link: String) {
    _whatsappBuyLink.value = link
    saveToDisk()
  }

  fun addGiveaway(title: String, desc: String, link: String): Result<Giveaway> {
    if (title.isBlank() || link.isBlank()) {
      return Result.failure(IllegalArgumentException("Title and link are required"))
    }
    val item = Giveaway(
      id = System.currentTimeMillis(),
      title = title.trim(),
      description = desc.trim(),
      linkUrl = link.trim()
    )
    _giveaways.value = listOf(item) + _giveaways.value
    saveToDisk()
    return Result.success(item)
  }

  fun deleteGiveaway(id: Long) {
    _giveaways.value = _giveaways.value.filter { it.id != id }
    saveToDisk()
  }

  fun adminGrant(targetIdentifier: String, coins: Long, vipDays: Int): Result<User> {
    val trimmed = targetIdentifier.trim()
    val target = _users.value.find {
      it.id.toString() == trimmed || it.name.equals(trimmed, ignoreCase = true)
    } ?: return Result.failure(IllegalArgumentException("Player '$trimmed' not found"))

    var updated = target
    if (coins != 0L) {
      updated = updated.copy(balance = (updated.balance + coins).coerceAtLeast(0L))
      addTransaction(target.id, "Admin granted ${formatCoins(coins)} coins", coins)
    }
    if (vipDays > 0) {
      val addMs = vipDays.toLong() * 24 * 60 * 60 * 1000L
      val base = if ((updated.vipUntil ?: 0L) > System.currentTimeMillis()) updated.vipUntil!! else System.currentTimeMillis()
      updated = updated.copy(vipUntil = base + addMs)
      addTransaction(target.id, "Admin granted $vipDays VIP days", 0L)
    }

    updateUser(updated)
    saveToDisk()
    return Result.success(updated)
  }

  fun adminToggleBan(userId: Long): Result<Boolean> {
    val user = _users.value.find { it.id == userId }
      ?: return Result.failure(IllegalArgumentException("User not found"))
    val newBan = !user.isBanned
    updateUser(user.copy(isBanned = newBan))
    saveToDisk()
    return Result.success(newBan)
  }

  fun adminSetForcedTier(userId: Long, tierIndex: Int?) {
    val user = _users.value.find { it.id == userId } ?: return
    updateUser(user.copy(forcedTierIndex = tierIndex))
    saveToDisk()
  }

  // --- Preferences & Cloud Sync ---
  fun setThemeMode(mode: ThemeMode) {
    _themeMode.value = mode
    prefs.edit().putString("theme_mode", mode.name).apply()
  }

  fun setHapticsEnabled(enabled: Boolean) {
    _hapticsEnabled.value = enabled
    prefs.edit().putBoolean("haptics_enabled", enabled).apply()
  }

  fun triggerCloudSync() {
    _isCloudSyncing.value = true
    // Simulate instantaneous asynchronous cloud sync
    val now = System.currentTimeMillis()
    _lastSyncedTimestamp.value = now
    prefs.edit().putLong("last_cloud_synced", now).apply()
    _isCloudSyncing.value = false
  }

  fun exportCloudSyncCode(): String {
    val json = JSONObject().apply {
      put("version", 1)
      put("timestamp", System.currentTimeMillis())
      put("usersCount", _users.value.size)
      put("stats_vol", _globalStats.value.totalVolume)
      put("stats_fee", _globalStats.value.totalFeeCollected)
      put("stats_dep", _globalStats.value.totalDeposits)
      val userArr = JSONArray()
      _users.value.forEach { u ->
        userArr.put(JSONObject().apply {
          put("id", u.id)
          put("name", u.name)
          put("pass", u.pass)
          put("role", u.role)
          put("bal", u.balance)
          put("wag", u.wagered)
          put("dep", u.deposited)
          put("vipUntil", u.vipUntil ?: 0L)
        })
      }
      put("users", userArr)
    }
    return android.util.Base64.encodeToString(
      json.toString().toByteArray(Charsets.UTF_8),
      android.util.Base64.NO_WRAP
    )
  }

  fun importCloudSyncCode(syncCode: String): Result<Int> {
    return try {
      val decoded = String(android.util.Base64.decode(syncCode.trim(), android.util.Base64.DEFAULT), Charsets.UTF_8)
      val json = JSONObject(decoded)
      val usersArr = json.getJSONArray("users")
      val importedUsers = mutableListOf<User>()

      for (i in 0 until usersArr.length()) {
        val obj = usersArr.getJSONObject(i)
        importedUsers.add(
          User(
            id = obj.getLong("id"),
            name = obj.getString("name"),
            pass = obj.getString("pass"),
            role = obj.optString("role", "player"),
            balance = obj.optLong("bal", 1000L),
            wagered = obj.optLong("wag", 0L),
            deposited = obj.optLong("dep", 0L),
            vipUntil = if (obj.optLong("vipUntil", 0L) > 0) obj.getLong("vipUntil") else null
          )
        )
      }

      if (importedUsers.isNotEmpty()) {
        _users.value = importedUsers
        _lastSyncedTimestamp.value = System.currentTimeMillis()
        saveToDisk()
        Result.success(importedUsers.size)
      } else {
        Result.failure(IllegalArgumentException("Sync code contained no user profiles"))
      }
    } catch (e: Exception) {
      Result.failure(IllegalArgumentException("Invalid or corrupted cloud sync code: ${e.message}"))
    }
  }

  // --- Private Helpers ---
  private fun updateUser(user: User) {
    _users.value = _users.value.map { if (it.id == user.id) user else it }
  }

  private fun addTransaction(userId: Long, description: String, amountChange: Long) {
    val key = "tx_list_$userId"
    val existing = prefs.getString(key, "[]") ?: "[]"
    val arr = JSONArray(existing)
    val item = JSONObject().apply {
      put("id", System.currentTimeMillis())
      put("t", System.currentTimeMillis())
      put("x", description)
      put("a", amountChange)
    }
    val updatedArr = JSONArray()
    updatedArr.put(item)
    for (i in 0 until min(arr.length(), 40)) {
      updatedArr.put(arr.getJSONObject(i))
    }
    prefs.edit().putString(key, updatedArr.toString()).apply()
  }

  fun getTransactions(userId: Long): List<TransactionRecord> {
    val key = "tx_list_$userId"
    val existing = prefs.getString(key, "[]") ?: "[]"
    val list = mutableListOf<TransactionRecord>()
    try {
      val arr = JSONArray(existing)
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        list.add(
          TransactionRecord(
            id = obj.getLong("id"),
            timestamp = obj.getLong("t"),
            description = obj.getString("x"),
            amountChange = obj.optLong("a", 0L)
          )
        )
      }
    } catch (_: Exception) {}
    return list
  }

  private fun loadFromDisk() {
    val themeStr = prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name
    _themeMode.value = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.DARK)
    _hapticsEnabled.value = prefs.getBoolean("haptics_enabled", true)
    _lastSyncedTimestamp.value = prefs.getLong("last_cloud_synced", System.currentTimeMillis())

    // If first launch, seed default users and games
    val isSeeded = prefs.getBoolean("app_seeded_v2", false)
    if (!isSeeded) {
      seedInitialData()
      prefs.edit().putBoolean("app_seeded_v2", true).apply()
    } else {
      loadSavedUsers()
    }
  }

  private fun seedInitialData() {
    val admin = User(
      id = 1L,
      name = "admin",
      pass = "2+2=5:-)",
      role = "admin",
      balance = 0L,
      wagered = 0L
    )

    val defaultPlayer = User(
      id = 20L,
      name = "player",
      pass = "pass123",
      role = "player",
      balance = 0L,
      wagered = 0L,
      friendIds = emptyList()
    )

    _users.value = listOf(admin, defaultPlayer)
    _currentUserId.value = defaultPlayer.id

    // No bot games in lobby
    _openGames.value = emptyList()

    // Initial giveaways
    _giveaways.value = listOf(
      Giveaway(
        id = 1L,
        title = "Weekend 10,000 Coin Mega Giveaway",
        description = "Join the community group for the live draw. Winners announced every Sunday night.",
        linkUrl = "https://chat.whatsapp.com/your-giveaway-group"
      ),
      Giveaway(
        id = 2L,
        title = "VIP High-Roller 25,000 Coin Airdrop",
        description = "Exclusive monthly prize pool distributed among active VIP members.",
        linkUrl = "https://chat.whatsapp.com/your-giveaway-group"
      )
    )

    // Initial notifications (no free coin promos)
    _notifications.value = listOf(
      AppNotification(
        id = "welcome_alert",
        title = "Welcome to Coin Flip! 🪙",
        message = "Real-player matchmaking active. Create a game or join live players in the lobby.",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.SYSTEM
      )
    )

    saveToDisk()
  }

  private fun saveToDisk() {
    val arr = JSONArray()
    _users.value.forEach { u ->
      val obj = JSONObject().apply {
        put("id", u.id)
        put("name", u.name)
        put("pass", u.pass)
        put("role", u.role)
        put("bal", u.balance)
        put("wag", u.wagered)
        put("dep", u.deposited)
        put("vipUntil", u.vipUntil ?: 0L)
        put("forcedTier", u.forcedTierIndex ?: -1)
        put("isBanned", u.isBanned)
        val fArr = JSONArray()
        u.friendIds.forEach { fArr.put(it) }
        put("friends", fArr)
      }
      arr.put(obj)
    }

    prefs.edit()
      .putString("saved_users_json", arr.toString())
      .putLong("current_user_id", _currentUserId.value ?: -1L)
      .apply()
  }

  private fun loadSavedUsers() {
    val json = prefs.getString("saved_users_json", null) ?: return
    try {
      val arr = JSONArray(json)
      val loaded = mutableListOf<User>()
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val fArr = obj.optJSONArray("friends") ?: JSONArray()
        val friendIds = mutableListOf<Long>()
        for (j in 0 until fArr.length()) {
          friendIds.add(fArr.getLong(j))
        }
        val forced = obj.optInt("forcedTier", -1)
        loaded.add(
          User(
            id = obj.getLong("id"),
            name = obj.getString("name"),
            pass = obj.getString("pass"),
            role = obj.optString("role", "player"),
            balance = obj.optLong("bal", 1000L),
            wagered = obj.optLong("wag", 0L),
            deposited = obj.optLong("dep", 0L),
            vipUntil = if (obj.optLong("vipUntil", 0L) > 0) obj.getLong("vipUntil") else null,
            forcedTierIndex = if (forced in 0..4) forced else null,
            isBanned = obj.optBoolean("isBanned", false),
            friendIds = friendIds
          )
        )
      }
      _users.value = loaded
      val cid = prefs.getLong("current_user_id", -1L)
      if (cid != -1L) {
        _currentUserId.value = cid
      } else {
        _currentUserId.value = loaded.firstOrNull()?.id
      }
    } catch (_: Exception) {}
  }

  private fun formatCoins(amount: Long): String {
    return String.format("%,d", amount)
  }
}
