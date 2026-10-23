package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AppNotification
import com.example.model.NotificationType

class PushNotificationManager(private val context: Context) {

  companion object {
    const val CHANNEL_GAMES = "coin_flip_games_channel"
    const val CHANNEL_REWARDS = "coin_flip_rewards_channel"
    const val CHANNEL_COMMUNITY = "coin_flip_community_channel"
  }

  init {
    createChannels()
  }

  private fun createChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val gameChannel = NotificationChannel(
        CHANNEL_GAMES,
        "Game Challenges & Wins",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts for coin flip invites, challenges, and match outcomes"
        enableVibration(true)
      }

      val rewardChannel = NotificationChannel(
        CHANNEL_REWARDS,
        "Rewards & Daily Bonuses",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Reminders to claim daily free coins and VIP perks"
      }

      val communityChannel = NotificationChannel(
        CHANNEL_COMMUNITY,
        "Giveaways & Community",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Announcements of weekend jackpot giveaways and tournaments"
      }

      notificationManager.createNotificationChannels(
        listOf(gameChannel, rewardChannel, communityChannel)
      )
    }
  }

  fun hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun sendSystemNotification(notification: AppNotification) {
    if (!hasNotificationPermission()) return

    val channelId = when (notification.type) {
      NotificationType.GAME_CHALLENGE, NotificationType.GAME_WIN -> CHANNEL_GAMES
      NotificationType.VIP_PERK -> CHANNEL_REWARDS
      NotificationType.COMMUNITY, NotificationType.SYSTEM -> CHANNEL_COMMUNITY
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      System.currentTimeMillis().toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle(notification.title)
      .setContentText(notification.message)
      .setPriority(
        if (notification.type == NotificationType.GAME_WIN)
          NotificationCompat.PRIORITY_HIGH
        else
          NotificationCompat.PRIORITY_DEFAULT
      )
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)

    try {
      val manager = NotificationManagerCompat.from(context)
      val notificationId = (System.currentTimeMillis() % 10000).toInt()
      manager.notify(notificationId, builder.build())
    } catch (_: SecurityException) {
      // Permission not granted or restricted
    }
  }
}
