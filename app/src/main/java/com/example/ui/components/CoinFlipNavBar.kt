package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNavTab
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary

@Composable
fun CoinFlipNavBar(
  currentTab: AppNavTab,
  onTabSelected: (AppNavTab) -> Unit,
  isAdmin: Boolean,
  modifier: Modifier = Modifier
) {
  val tabs = mutableListOf(
    AppNavTab.PLAY,
    AppNavTab.WALLET,
    AppNavTab.RANKS,
    AppNavTab.FRIENDS,
    AppNavTab.GIVEAWAYS
  )
  if (isAdmin) {
    tabs.add(AppNavTab.ADMIN)
  }

  NavigationBar(
    modifier = modifier.navigationBarsPadding(),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp
  ) {
    tabs.forEach { tab ->
      val isSelected = currentTab == tab

      val (filledIcon, outlinedIcon) = when (tab) {
        AppNavTab.PLAY -> Icons.Filled.Casino to Icons.Outlined.Casino
        AppNavTab.WALLET -> Icons.Filled.AccountBalanceWallet to Icons.Outlined.AccountBalanceWallet
        AppNavTab.RANKS -> Icons.Filled.EmojiEvents to Icons.Outlined.EmojiEvents
        AppNavTab.FRIENDS -> Icons.Filled.Group to Icons.Outlined.Group
        AppNavTab.GIVEAWAYS -> Icons.Filled.CardGiftcard to Icons.Outlined.CardGiftcard
        AppNavTab.ADMIN -> Icons.Filled.AdminPanelSettings to Icons.Outlined.AdminPanelSettings
      }

      NavigationBarItem(
        selected = isSelected,
        onClick = { onTabSelected(tab) },
        icon = {
          Icon(
            imageVector = if (isSelected) filledIcon else outlinedIcon,
            contentDescription = tab.label,
            modifier = Modifier.size(20.dp)
          )
        },
        label = {
          Text(
            text = tab.label,
            fontFamily = ChakraPetchFont,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = GoldPrimary,
          selectedTextColor = GoldPrimary,
          indicatorColor = GoldContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
      )
    }
  }
}
