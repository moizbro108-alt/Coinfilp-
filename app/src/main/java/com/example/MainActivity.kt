package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppNavTab
import com.example.model.ThemeMode
import com.example.ui.components.AuthDialog
import com.example.ui.components.CoinFlipHeader
import com.example.ui.components.CoinFlipNavBar
import com.example.ui.components.DownloadAppDialog
import com.example.ui.components.NotificationsDrawer
import com.example.ui.components.SettingsAndCloudDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GiveawaysScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.RanksScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CoinFlipViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: CoinFlipViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsState()

      // Determine dark mode based on theme settings
      val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
      }

      // Request notification permission on Android 13+
      val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
      ) { /* Permission result handled gracefully */ }

      LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
      }

      MyApplicationTheme(darkTheme = isDark) {
        CoinFlipApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun CoinFlipApp(
  viewModel: CoinFlipViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val currentTab by viewModel.currentTab.collectAsState()
  val isSyncing by viewModel.isCloudSyncing.collectAsState()
  val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()

  val showSettings by viewModel.showSettingsDialog.collectAsState()
  val showNotifications by viewModel.showNotificationsSheet.collectAsState()
  val showAuth by viewModel.showAuthDialog.collectAsState()
  val showDownload by viewModel.showDownloadDialog.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) {
    viewModel.toastMessage.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      CoinFlipHeader(
        user = currentUser,
        isSyncing = isSyncing,
        unreadCount = unreadNotifications,
        onOpenNotifications = { viewModel.openNotifications(true) },
        onOpenDownload = { viewModel.openDownload(true) },
        onOpenSettings = { viewModel.openSettings(true) },
        onOpenCloudSync = { viewModel.openSettings(true) },
        onOpenAuth = { viewModel.openAuth(true) }
      )
    },
    bottomBar = {
      CoinFlipNavBar(
        currentTab = currentTab,
        onTabSelected = { viewModel.selectTab(it) },
        isAdmin = currentUser?.role == "admin"
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        AppNavTab.PLAY -> PlayScreen(viewModel = viewModel)
        AppNavTab.WALLET -> WalletScreen(viewModel = viewModel)
        AppNavTab.RANKS -> RanksScreen(viewModel = viewModel)
        AppNavTab.FRIENDS -> FriendsScreen(viewModel = viewModel)
        AppNavTab.GIVEAWAYS -> GiveawaysScreen(viewModel = viewModel)
        AppNavTab.ADMIN -> AdminScreen(viewModel = viewModel)
      }
    }
  }

  // Modals & Dialogs
  if (showSettings) {
    SettingsAndCloudDialog(
      viewModel = viewModel,
      onDismiss = { viewModel.openSettings(false) }
    )
  }

  if (showNotifications) {
    NotificationsDrawer(
      viewModel = viewModel,
      onDismiss = { viewModel.openNotifications(false) }
    )
  }

  if (showAuth) {
    AuthDialog(
      viewModel = viewModel,
      onDismiss = { viewModel.openAuth(false) }
    )
  }

  if (showDownload) {
    DownloadAppDialog(
      viewModel = viewModel,
      onDismiss = { viewModel.openDownload(false) }
    )
  }
}
