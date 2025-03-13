package com.example.fyp_androidapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.fyp_androidapp.data.models.TableItem

@Composable
fun BottomTabBar(
    tabs: List<TableItem>,
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar {
        tabs.forEach { tab ->
            val isSelected = currentRoute == tab.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary, // Selected icon color
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Unselected icon color
                    selectedTextColor = MaterialTheme.colorScheme.primary, // Selected text color
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), // Unselected text color
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer // Indicator (background) color for selected tab
                )
            )
        }
    }
}
