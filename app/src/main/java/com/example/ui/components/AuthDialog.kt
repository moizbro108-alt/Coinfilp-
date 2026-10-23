package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChakraPetchFont
import com.example.ui.theme.GoldPrimary
import com.example.viewmodel.CoinFlipViewModel

@Composable
fun AuthDialog(
  viewModel: CoinFlipViewModel,
  onDismiss: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val allUsers by viewModel.users.collectAsState()

  var isSignUp by remember { mutableStateOf(false) }
  var usernameInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", fontFamily = ChakraPetchFont, fontWeight = FontWeight.Bold)
      }
    },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.AccountCircle,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentUser != null) "User Account & Switcher" else "Log In / Sign Up",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Quick Account Switcher (allows switching between test accounts with 1 tap)
        Text(
          text = "QUICK ACCOUNT SWITCHER",
          fontFamily = ChakraPetchFont,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          allUsers.forEach { u ->
            val isCurrent = u.id == currentUser?.id
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  viewModel.switchUser(u.id)
                  onDismiss()
                },
              color = if (isCurrent) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(10.dp),
              border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else null
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = u.name,
                      fontFamily = ChakraPetchFont,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                    if (u.role == "admin") {
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "ADMIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                      )
                    }
                  }
                  Text(
                    text = "${viewModel.formatCoins(u.balance)} coins • ${u.tier.title}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                if (isCurrent) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Mode switch tabs
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!isSignUp) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
              .border(
                width = 1.dp,
                color = if (!isSignUp) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
              )
              .clickable {
                isSignUp = false
                errorMessage = null
              }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Log In",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              color = if (!isSignUp) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSignUp) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
              .border(
                width = 1.dp,
                color = if (isSignUp) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
              )
              .clickable {
                isSignUp = true
                errorMessage = null
              }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Sign Up",
              fontFamily = ChakraPetchFont,
              fontWeight = FontWeight.Bold,
              color = if (isSignUp) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        OutlinedTextField(
          value = usernameInput,
          onValueChange = { usernameInput = it },
          label = { Text("Username") },
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
          modifier = Modifier.fillMaxWidth().testTag("auth_username_input"),
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = passwordInput,
          onValueChange = { passwordInput = it },
          label = { Text("Password") },
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            }
          },
          modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
          shape = RoundedCornerShape(10.dp)
        )

        if (errorMessage != null) {
          Text(
            text = errorMessage!!,
            color = Color(0xFFEF4444),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Button(
          onClick = {
            if (isSignUp) {
              viewModel.signup(
                usernameInput,
                passwordInput,
                onSuccess = { onDismiss() },
                onError = { errorMessage = it }
              )
            } else {
              viewModel.login(
                usernameInput,
                passwordInput,
                onSuccess = { onDismiss() },
                onError = { errorMessage = it }
              )
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF221600)
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().height(46.dp).testTag("auth_submit_button")
        ) {
          Text(
            text = if (isSignUp) "Create Account" else "Log In",
            fontFamily = ChakraPetchFont,
            fontWeight = FontWeight.Bold
          )
        }

        if (currentUser != null) {
          OutlinedButton(
            onClick = {
              viewModel.logout()
              onDismiss()
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Log Out", fontFamily = ChakraPetchFont, color = Color(0xFFEF4444))
          }
        }
      }
    }
  )
}
