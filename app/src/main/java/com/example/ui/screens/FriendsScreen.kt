package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.User
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.CoinFlipViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FriendsScreen(
  viewModel: CoinFlipViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val allUsers by viewModel.users.collectAsState()
  val activeChatFriend by viewModel.activeChatFriend.collectAsState()

  if (activeChatFriend != null && currentUser != null) {
    DirectChatView(
      me = currentUser!!,
      friend = activeChatFriend!!,
      viewModel = viewModel,
      onBack = { viewModel.closeChat() }
    )
    return
  }

  var addFriendInput by remember { mutableStateOf("") }
  val friendIds = currentUser?.friendIds ?: emptyList()
  val friendUsers = remember(friendIds, allUsers) {
    allUsers.filter { friendIds.contains(it.id) }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Add Friend Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "ADD A FRIEND",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Your Player ID is #${currentUser?.id ?: 0}. Share it with friends to connect!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = addFriendInput,
              onValueChange = { addFriendInput = it },
              placeholder = { Text("Player Name or ID") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("add_friend_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
              ),
              shape = RoundedCornerShape(12.dp)
            )

            Button(
              onClick = {
                viewModel.addFriend(addFriendInput)
                addFriendInput = ""
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color(0xFF221600)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.height(52.dp).testTag("add_friend_button")
            ) {
              Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = "Add", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Friends List Section Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "YOUR FRIENDS (${friendUsers.size})",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onBackground
        )

        Text(
          text = "Invites use your selected Play stake",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (friendUsers.isEmpty()) {
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
            Text(text = "👥", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "No friends added yet",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Add friends to chat, send direct challenges, and gift coins!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(friendUsers, key = { it.id }) { friend ->
        FriendItemCard(
          friend = friend,
          onChat = { viewModel.openChat(friend) },
          onInvite = { viewModel.inviteFriendToMatch(friend.id) },
          onRemove = { viewModel.removeFriend(friend.id) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
fun FriendItemCard(
  friend: User,
  onChat: () -> Unit,
  onInvite: () -> Unit,
  onRemove: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(GoldPrimary.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = friend.name.take(1).uppercase(),
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            fontSize = 16.sp
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = friend.name,
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "#${friend.id} • ${friend.tier.title} Tier",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
          onClick = onChat,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("chat_friend_${friend.id}")
        ) {
          Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(text = "Chat", fontSize = 11.sp, fontFamily = ChakraPetchFont)
        }

        Button(
          onClick = onInvite,
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF221600)
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("invite_friend_${friend.id}")
        ) {
          Icon(imageVector = Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(text = "Invite", fontSize = 11.sp, fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
        }

        IconButton(
          onClick = onRemove,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remove",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
fun DirectChatView(
  me: User,
  friend: User,
  viewModel: CoinFlipViewModel,
  onBack: () -> Unit
) {
  val messages = viewModel.repository.getChatConversation(me.id, friend.id)
  var messageInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // Chat Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
      }
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = friend.name,
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
        Text(
          text = "ID #${friend.id} • ${friend.tier.title} Tier",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }

    // Message List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (messages.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No messages yet. Say hello to ${friend.name}!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        items(messages, key = { it.id }) { msg ->
          val isMe = msg.fromUserId == me.id
          val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
          ) {
            Surface(
              color = if (isMe) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
              contentColor = if (isMe) Color(0xFF221600) else MaterialTheme.colorScheme.onSurface,
              shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMe) 14.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 14.dp
              ),
              modifier = Modifier.widthIn(max = 280.dp)
            ) {
              Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                  text = msg.message,
                  fontSize = 14.sp
                )
                Text(
                  text = timeStr,
                  fontSize = 10.sp,
                  color = if (isMe) Color(0x99221600) else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
              }
            }
          }
        }
      }
    }

    // Message Input
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = messageInput,
        onValueChange = { messageInput = it },
        placeholder = { Text("Type a message...") },
        singleLine = true,
        modifier = Modifier.weight(1f).testTag("chat_input_field"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(20.dp)
      )

      Button(
        onClick = {
          if (messageInput.isNotBlank()) {
            viewModel.sendChat(messageInput)
            messageInput = ""
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldPrimary,
          contentColor = Color(0xFF221600)
        ),
        shape = CircleShape,
        modifier = Modifier.size(48.dp).testTag("send_chat_button")
      ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
      }
    }
  }
}
