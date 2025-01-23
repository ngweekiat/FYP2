
package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = { CustomTopBar(title = "Manage Accounts") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Accounts",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Singular card for all accounts
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Google Calendar Account
                    AccountItem(
                        accountType = "Google Calendar",
                        accountEmail = "ngweekiat520@gmail.com",
                        onUnlinkClick = { /* Handle Google unlink logic */ }
                    )

                    // Divider for Google Calendar
                    Divider(
                        color = Color.Black,
                        thickness = 1.dp,
                        modifier = Modifier.padding(start = 72.dp) // Align with account text start
                    )

                    // Outlook Calendar Account
                    AccountItem(
                        accountType = "Outlook Calendar",
                        accountEmail = "ngwe0105@e.ntu.edu.sg",
                        onUnlinkClick = { /* Handle Outlook unlink logic */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Handle link account logic */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Link Account")
            }
        }
    }
}

@Composable
fun CustomTopBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
fun AccountItem(accountType: String, accountEmail: String, onUnlinkClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for the logo
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "L", // Replace with an Image or Icon when available
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = accountType,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = accountEmail,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp) // Custom padding inside the button
                    .clickable { onUnlinkClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unlink Calendar",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
