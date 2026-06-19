package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HealthViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: HealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full Edge-to-Edge support
        enableEdgeToEdge()
        
        setContent {
            val profile by viewModel.userProfile.collectAsState()
            val selectedTab by viewModel.selectedTab.collectAsState()
            
            // Adapt app theme dynamically based on user setting selection
            val useDarkTheme = profile.theme == "มืด"
            
            MyApplicationTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            DiaCareBottomBar(
                                selectedTab = selectedTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("app_scaffold")
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // High-fidelity smooth fade transition between screen states
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "ScreenTransition"
                            ) { tab ->
                                when (tab) {
                                    "HOME" -> HomeScreen(viewModel = viewModel)
                                    "GLUCOSE" -> GlucoseScreen(viewModel = viewModel)
                                    "MEDS" -> MedicineScreen(viewModel = viewModel)
                                    "DOCTOR" -> DoctorScreen(viewModel = viewModel)
                                    "REPORT" -> ReportScreen(viewModel = viewModel)
                                    "SETTINGS" -> SettingsScreen(viewModel = viewModel)
                                    else -> HomeScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiaCareBottomBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    // 6 Navigation Options to represent the fully detailed system mockup
    val items = listOf(
        NavigationItem("HOME", "หน้าแรก", Icons.Default.Home, "tab_home"),
        NavigationItem("GLUCOSE", "น้ำตาล", Icons.Default.Bloodtype, "tab_glucose"),
        NavigationItem("MEDS", "ยา", Icons.Default.MedicalServices, "tab_meds"),
        NavigationItem("DOCTOR", "นัดหมอ", Icons.Default.CalendarMonth, "tab_doctor"),
        NavigationItem("REPORT", "รายงาน", Icons.Default.Analytics, "tab_report"),
        NavigationItem("SETTINGS", "ตั้งค่า", Icons.Default.Settings, "tab_settings")
    )

    NavigationBar(
        tonalElevation = 0.dp, // Flat clean look
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        items.forEach { item ->
            val isSelected = selectedTab == item.id
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.id) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondary,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

data class NavigationItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
