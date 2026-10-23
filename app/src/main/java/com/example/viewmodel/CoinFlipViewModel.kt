package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CoinFlipRepository
import com.example.model.AppNavTab
import com.example.model.AppNotification
import com.example.model.ChatMessage
import com.example.model.CoinSide
import com.example.model.DepositAlert
import com.example.model.GameHistoryItem
import com.example.model.GameOffer
import com.example.model.Giveaway
import com.example.model.GlobalStats
import com.example.model.NotificationType
import com.example.model.ThemeMode
import com.example.model.TransactionRecord
import com.example.model.User
import com.example.notifications.PushNotificationManager
import com.example.util.HapticFeedbackHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LeaderboardEntry(
  val rank: Int,
  val userName: String,
  val valueText: String,
  val tierTitle: String
)

class CoinFlipViewModel(application: Application) : AndroidViewModel(application) {

  val repository = CoinFlipRepository(application)
  private val pushManager = PushNotificationManager(application)
  val haptics = HapticFeedbackHelper(application)

  // Current tab
  private val _currentTab = MutableStateFlow(AppNavTab.PLAY)
  val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

  // Selected game parameters in Play Tab
  private val _selectedSide = MutableStateFlow(CoinSide.HEADS)
  val selectedSide: StateFlow<CoinSide> = _selectedSide.asStateFlow()

  private val _selectedStake = MutableStateFlow(500L)
  val selectedStake: StateFlow<Long> = _selectedStake.asStateFlow()

  // 3D Coin Animation States
  private val _isFlipping = MutableStateFlow(false)
  val isFlipping: StateFlow<Boolean> = _isFlipping.asStateFlow()

  private val _coinRotationY = MutableStateFlow(0f)
  val coinRotationY: StateFlow<Float> = _coinRotationY.asStateFlow()

  private val _coinLiftFraction = MutableStateFlow(0f)
  val coinLiftFraction: StateFlow<Float> = _coinLiftFraction.asStateFlow()

  private val _lastGameOutcome = MutableStateFlow<CoinFlipRepository.PlayOutcome?>(null)
  val lastGameOutcome: StateFlow<CoinFlipRepository.PlayOutcome?> = _lastGameOutcome.asStateFlow()

  private val _gameStatusMessage = MutableStateFlow("Pick Heads or Tails, set your stake, and flip!")
  val gameStatusMessage: StateFlow<String> = _gameStatusMessage.asStateFlow()

  // Active chat recipient
  private val _activeChatFriend = MutableStateFlow<User?>(null)
  val activeChatFriend: StateFlow<User?> = _activeChatFriend.asStateFlow()

  // Dialog & Modal Visibilities
  private val _showSettingsDialog = MutableStateFlow(false)
  val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

  private val _showNotificationsSheet = MutableStateFlow(false)
  val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

  private val _showCloudSyncDialog = MutableStateFlow(false)
  val showCloudSyncDialog: StateFlow<Boolean> = _showCloudSyncDialog.asStateFlow()

  private val _showAuthDialog = MutableStateFlow(false)
  val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

  private val _showDownloadDialog = MutableStateFlow(false)
  val showDownloadDialog: StateFlow<Boolean> = _showDownloadDialog.asStateFlow()

  // Toast / Snack events
  private val _toastMessage = MutableSharedFlow<String>()
  val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

  // Leaderboard time filter
  private val _leaderboardPeriod = MutableStateFlow("24h")
  val leaderboardPeriod: StateFlow<String> = _leaderboardPeriod.asStateFlow()

  // Reactive state derived from repository
  val users: StateFlow<List<User>> = repository.users
  val currentUserId: StateFlow<Long?> = repository.currentUserId
  val openGames: StateFlow<List<GameOffer>> = repository.openGames
  val gameHistory: StateFlow<List<GameHistoryItem>> = repository.gameHistory
  val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
  val giveaways: StateFlow<List<Giveaway>> = repository.giveaways
  val notifications: StateFlow<List<AppNotification>> = repository.notifications
  val depositAlerts: StateFlow<List<DepositAlert>> = repository.depositAlerts
  val globalStats: StateFlow<GlobalStats> = repository.globalStats
  val whatsappBuyLink: StateFlow<String> = repository.whatsappBuyLink
  val themeMode: StateFlow<ThemeMode> = repository.themeMode
  val isCloudSyncing: StateFlow<Boolean> = repository.isCloudSyncing
  val lastSyncedTimestamp: StateFlow<Long> = repository.lastSyncedTimestamp
  val hapticsEnabled: StateFlow<Boolean> = repository.hapticsEnabled

  val currentUser: StateFlow<User?> = repository.users.map { list ->
    val id = repository.currentUserId.value
    list.find { it.id == id }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, repository.getCurrentUser())

  val unreadNotificationsCount: StateFlow<Int> = repository.notifications.map { list ->
    list.count { !it.isRead }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  fun selectTab(tab: AppNavTab) {
    _currentTab.value = tab
    _activeChatFriend.value = null
  }

  fun selectSide(side: CoinSide) {
    _selectedSide.value = side
    triggerHapticTick()
  }

  fun setStake(stake: Long) {
    _selectedStake.value = stake.coerceAtLeast(100L)
    triggerHapticTick()
  }

  fun createGame() {
    val user = currentUser.value ?: return
    val stake = _selectedStake.value
    if (stake > user.balance) {
      postToast("Not enough coins! Check your wallet.")
      return
    }

    val result = repository.createGame(user.id, _selectedSide.value, stake)
    result.onSuccess {
      postToast("Game created for ${formatCoins(stake)} coins on ${_selectedSide.value.displayName}!")
      _gameStatusMessage.value = "Game open in lobby. Waiting for a challenger to join..."
      triggerHapticTick()
    }.onFailure { e ->
      postToast(e.message ?: "Could not create game")
    }
  }

  fun cancelGame(game: GameOffer) {
    val user = currentUser.value ?: return
    repository.cancelGame(game.id, user.id).onSuccess {
      postToast("Game cancelled. ${formatCoins(game.stake)} coins refunded.")
      triggerHapticTick()
    }
  }

  fun joinGame(game: GameOffer) {
    val user = currentUser.value ?: return
    if (_isFlipping.value) return

    if (user.balance < game.stake) {
      postToast("Insufficient balance to join ${formatCoins(game.stake)} stake")
      return
    }

    viewModelScope.launch {
      _isFlipping.value = true
      _gameStatusMessage.value = "Flipping coin..."
      triggerHapticSpin()

      // Calculate the outcome
      val outcomeResult = repository.executeGame(game.id, user.id)

      outcomeResult.onSuccess { outcome ->
        _lastGameOutcome.value = outcome

        // 3D Flip Animation Sequence:
        // Target angle based on outcome:
        // 0 mod 360 = HEADS; 180 mod 360 = TAILS
        val currentRot = _coinRotationY.value
        val fullRotations = (5 + Random.nextInt(3)) * 360f
        val targetMod = if (outcome.resultSide == CoinSide.HEADS) 0f else 180f
        val targetAngle = currentRot + fullRotations + targetMod

        val steps = 40
        for (i in 1..steps) {
          val progress = i / steps.toFloat()
          // Smooth ease-out cubic
          val t = 1f - progress
          val eased = 1f - (t * t * t)

          _coinRotationY.value = currentRot + (targetAngle - currentRot) * eased

          // Parabolic height lift
          val lift = 4f * progress * (1f - progress)
          _coinLiftFraction.value = lift

          if (i % 6 == 0) {
            triggerHapticTick()
          }
          delay(45)
        }

        _coinRotationY.value = targetAngle % 360f
        _coinLiftFraction.value = 0f
        _isFlipping.value = false

        if (outcome.isUserWinner) {
          triggerHapticWin()
          _gameStatusMessage.value = "🎉 It's ${outcome.resultSide.displayName}! You won ${formatCoins(outcome.settlement.payout)} coins!"
          postToast("You won ${formatCoins(outcome.settlement.payout)} coins!")

          // Send system push notification for big win
          val winNotif = AppNotification(
            id = "win_${System.currentTimeMillis()}",
            title = "Victory! +${formatCoins(outcome.settlement.payout)} Coins 🏆",
            message = "You won against ${outcome.loser.name} on ${outcome.resultSide.displayName}!",
            timestamp = System.currentTimeMillis(),
            type = NotificationType.GAME_WIN
          )
          pushManager.sendSystemNotification(winNotif)
          repository.pushNotification(winNotif)
        } else {
          triggerHapticLose()
          _gameStatusMessage.value = "It's ${outcome.resultSide.displayName}. ${outcome.winner.name} takes the pot."
          postToast("Lost ${formatCoins(game.stake)} coins vs ${outcome.winner.name}")
        }
      }.onFailure { e ->
        _isFlipping.value = false
        postToast(e.message ?: "Failed to join game")
      }
    }
  }

  fun playSoloPracticeFlip() {
    if (_isFlipping.value) return
    viewModelScope.launch {
      _isFlipping.value = true
      _gameStatusMessage.value = "Flipping practice coin..."
      triggerHapticSpin()

      val chosenSide = _selectedSide.value
      val resultSide = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
      val won = chosenSide == resultSide

      val currentRot = _coinRotationY.value
      val fullRotations = 5 * 360f
      val targetMod = if (resultSide == CoinSide.HEADS) 0f else 180f
      val targetAngle = currentRot + fullRotations + targetMod

      val steps = 35
      for (i in 1..steps) {
        val progress = i / steps.toFloat()
        val t = 1f - progress
        val eased = 1f - (t * t * t)
        _coinRotationY.value = currentRot + (targetAngle - currentRot) * eased
        _coinLiftFraction.value = 4f * progress * (1f - progress)
        if (i % 6 == 0) triggerHapticTick()
        delay(40)
      }

      _coinRotationY.value = targetAngle % 360f
      _coinLiftFraction.value = 0f
      _isFlipping.value = false

      if (won) {
        triggerHapticWin()
        _gameStatusMessage.value = "🎉 It's ${resultSide.displayName}! Practice win on ${chosenSide.displayName}!"
      } else {
        triggerHapticLose()
        _gameStatusMessage.value = "It's ${resultSide.displayName}. Better luck next flip!"
      }
    }
  }

  // --- Wallet ---
  fun depositCoins(amount: Long) {
    val user = currentUser.value ?: return
    repository.depositCoins(user.id, amount).onSuccess {
      triggerHapticTick()
      postToast("Deposited ${formatCoins(amount)} coins. Admin notified!")
    }.onFailure { e ->
      postToast(e.message ?: "Deposit failed")
    }
  }

  fun giftCoins(targetIdentifier: String, amount: Long) {
    val user = currentUser.value ?: return
    repository.giftCoins(user.id, targetIdentifier, amount).onSuccess { target ->
      triggerHapticTick()
      postToast("Sent ${formatCoins(amount)} coins to ${target.name}!")
    }.onFailure { e ->
      postToast(e.message ?: "Could not gift coins")
    }
  }

  fun buyVip() {
    val user = currentUser.value ?: return
    repository.buyVip(user.id).onSuccess {
      triggerHapticWin()
      postToast("VIP Membership activated for 30 days! Commission reduced by 2%.")
    }.onFailure { e ->
      postToast(e.message ?: "Could not purchase VIP")
    }
  }

  // --- Friends & Chat ---
  fun addFriend(targetIdentifier: String) {
    val user = currentUser.value ?: return
    repository.addFriend(user.id, targetIdentifier).onSuccess { friend ->
      triggerHapticTick()
      postToast("Added ${friend.name} as a friend!")
    }.onFailure { e ->
      postToast(e.message ?: "Could not add friend")
    }
  }

  fun removeFriend(friendId: Long) {
    val user = currentUser.value ?: return
    repository.removeFriend(user.id, friendId)
    postToast("Friend removed")
  }

  fun openChat(friend: User) {
    _activeChatFriend.value = friend
  }

  fun closeChat() {
    _activeChatFriend.value = null
  }

  fun sendChat(messageText: String) {
    val me = currentUser.value ?: return
    val friend = _activeChatFriend.value ?: return
    repository.sendChatMessage(me.id, friend.id, messageText).onSuccess {
      triggerHapticTick()
    }.onFailure { e ->
      postToast(e.message ?: "Message failed")
    }
  }

  fun inviteFriendToMatch(friendId: Long) {
    val me = currentUser.value ?: return
    val stake = _selectedStake.value
    val side = _selectedSide.value
    repository.createGame(me.id, side, stake, invitedUserId = friendId).onSuccess {
      postToast("Match invite sent to friend for ${formatCoins(stake)} coins on ${side.displayName}!")
      _currentTab.value = AppNavTab.PLAY
    }.onFailure { e ->
      postToast(e.message ?: "Could not send challenge")
    }
  }

  // --- Giveaways ---
  fun addGiveaway(title: String, desc: String, link: String) {
    repository.addGiveaway(title, desc, link).onSuccess {
      postToast("Giveaway published!")
    }.onFailure { e ->
      postToast(e.message ?: "Error adding giveaway")
    }
  }

  fun deleteGiveaway(id: Long) {
    repository.deleteGiveaway(id)
    postToast("Giveaway removed")
  }

  // --- Admin ---
  fun adminGrant(target: String, coins: Long, vipDays: Int) {
    repository.adminGrant(target, coins, vipDays).onSuccess { updated ->
      postToast("Granted to ${updated.name}: ${formatCoins(coins)} coins, $vipDays VIP days")
    }.onFailure { e ->
      postToast(e.message ?: "Admin grant failed")
    }
  }

  fun adminToggleBan(userId: Long) {
    repository.adminToggleBan(userId).onSuccess { isBanned ->
      postToast(if (isBanned) "User suspended" else "User unbanned")
    }
  }

  fun adminSetTier(userId: Long, tierIdx: Int?) {
    repository.adminSetForcedTier(userId, tierIdx)
    postToast("User tier updated")
  }

  fun adminUpdateWhatsappLink(link: String) {
    repository.setWhatsappLink(link)
    postToast("Buy Coins link updated")
  }

  // --- Cloud Sync ---
  fun triggerCloudSync() {
    viewModelScope.launch {
      postToast("Synchronizing data with Cloud...")
      repository.triggerCloudSync()
      delay(400)
      postToast("Cloud Synchronization Complete! All profiles and matches synced.")
    }
  }

  fun exportCloudCode(): String {
    return repository.exportCloudSyncCode()
  }

  fun importCloudCode(code: String) {
    repository.importCloudSyncCode(code).onSuccess { count ->
      triggerHapticWin()
      postToast("Successfully restored $count player accounts from Cloud backup!")
      _showCloudSyncDialog.value = false
    }.onFailure { e ->
      postToast(e.message ?: "Cloud import failed")
    }
  }

  // --- Push Notifications Simulator & Management ---
  fun sendTestPushNotification(type: NotificationType) {
    val notif = when (type) {
      NotificationType.GAME_CHALLENGE -> AppNotification(
        id = "test_challenge_${System.currentTimeMillis()}",
        title = "🔥 Match Challenge Received",
        message = "A player challenged you to a 500 coin match on Tails. Accept now!",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.GAME_CHALLENGE
      )
      NotificationType.GAME_WIN -> AppNotification(
        id = "test_win_${System.currentTimeMillis()}",
        title = "🏆 Match Victory!",
        message = "You won your coin flip match! Payout credited to your balance.",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.GAME_WIN
      )
      NotificationType.VIP_PERK -> AppNotification(
        id = "test_vip_${System.currentTimeMillis()}",
        title = "👑 VIP Perk Unlocked",
        message = "Your commission has dropped to the minimum rate. Enjoy 2% extra profit!",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.VIP_PERK
      )
      NotificationType.COMMUNITY -> AppNotification(
        id = "test_community_${System.currentTimeMillis()}",
        title = "🎁 Weekend 10,000 Coin Giveaway",
        message = "Community raffle drawing starts in 1 hour. Tap to check tickets.",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.COMMUNITY
      )
      NotificationType.SYSTEM -> AppNotification(
        id = "test_sys_${System.currentTimeMillis()}",
        title = "⚡ Cloud Backup Synced",
        message = "Your coin balance and match logs are backed up securely across devices.",
        timestamp = System.currentTimeMillis(),
        type = NotificationType.SYSTEM
      )
    }

    repository.pushNotification(notif)
    pushManager.sendSystemNotification(notif)
    postToast("Push notification sent: ${notif.title}")
  }

  fun markNotificationsRead() {
    repository.markNotificationsAsRead()
  }

  fun clearNotifications() {
    repository.clearNotifications()
  }

  // --- Dialog Toggles ---
  fun openSettings(show: Boolean) { _showSettingsDialog.value = show }
  fun openNotifications(show: Boolean) { _showNotificationsSheet.value = show }
  fun openCloudSync(show: Boolean) { _showCloudSyncDialog.value = show }
  fun openAuth(show: Boolean) { _showAuthDialog.value = show }
  fun openDownload(show: Boolean) { _showDownloadDialog.value = show }

  fun setThemeMode(mode: ThemeMode) {
    repository.setThemeMode(mode)
  }

  fun setHapticsEnabled(enabled: Boolean) {
    repository.setHapticsEnabled(enabled)
  }

  fun setLeaderboardPeriod(period: String) {
    _leaderboardPeriod.value = period
  }

  // --- Auth ---
  fun login(username: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    repository.login(username, pass).onSuccess {
      postToast("Logged in as ${it.name}")
      _showAuthDialog.value = false
      onSuccess()
    }.onFailure {
      onError(it.message ?: "Login failed")
    }
  }

  fun signup(username: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    repository.signup(username, pass).onSuccess {
      postToast("Account created! Welcome ${it.name}")
      _showAuthDialog.value = false
      onSuccess()
    }.onFailure {
      onError(it.message ?: "Sign up failed")
    }
  }

  fun logout() {
    repository.logout()
    postToast("Logged out")
    _currentTab.value = AppNavTab.PLAY
  }

  fun switchUser(userId: Long) {
    val u = repository.users.value.find { it.id == userId } ?: return
    repository.login(u.name, u.pass)
    postToast("Switched to ${u.name}")
  }

  // --- Haptics helpers ---
  private fun triggerHapticTick() {
    if (hapticsEnabled.value) haptics.tick()
  }

  private fun triggerHapticSpin() {
    if (hapticsEnabled.value) haptics.flipSpin()
  }

  private fun triggerHapticWin() {
    if (hapticsEnabled.value) haptics.winCelebration()
  }

  private fun triggerHapticLose() {
    if (hapticsEnabled.value) haptics.losePulse()
  }

  private fun postToast(msg: String) {
    viewModelScope.launch {
      _toastMessage.emit(msg)
    }
  }

  fun formatCoins(amount: Long): String {
    return String.format("%,d", amount)
  }
}
