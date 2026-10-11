package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.Formatters
import com.example.ui.components.UserAvatar
import com.example.ui.screens.admin.*
import com.example.ui.screens.agent.*
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepository = AuthRepository(applicationContext)
        val microfinanceRepository = MicrofinanceRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                LayMicrofinanceApp(
                    authRepository = authRepository,
                    repository = microfinanceRepository
                )
            }
        }
    }
}

enum class AdminTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    AGENTS("Agents", Icons.Default.People),
    LOANS("Loans", Icons.Default.Assignment),
    LEDGER("Ledger", Icons.Default.ReceiptLong),
    ACCOUNT("Account", Icons.Default.Person)
}

enum class AgentTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CUSTOMERS("Customers", Icons.Default.People),
    COLLECTIONS("Collections", Icons.Default.Payments),
    SMS("SMS", Icons.Default.ChatBubble),
    ACCOUNT("Account", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayMicrofinanceApp(
    authRepository: AuthRepository,
    repository: MicrofinanceRepository
) {
    val currentUser by authRepository.currentUser.collectAsState()
    val notifications by repository.notifications.collectAsState()

    var adminSelectedTab by remember { mutableStateOf(AdminTab.HOME) }
    var agentSelectedTab by remember { mutableStateOf(AgentTab.HOME) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val unreadNotifs = notifications.count { !it.isRead }

    if (currentUser == null) {
        LoginScreen(
            authRepository = authRepository,
            onLoginSuccess = { user ->
                if (user.role == UserRole.ADMIN) {
                    adminSelectedTab = AdminTab.HOME
                } else {
                    agentSelectedTab = AgentTab.HOME
                }
            }
        )
    } else {
        val user = currentUser!!
        val isAdmin = user.role == UserRole.ADMIN

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.lay_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Lay Microfinance",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = LayNavyPrimary
                                    )
                                )
                                Text(
                                    text = if (isAdmin) "Admin Dashboard" else "Agent Portal • ${user.name}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = if (isAdmin) LayNavyLight else LayGreenDark
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        // Notification Bell with Badge
                        IconButton(
                            onClick = {
                                showNotificationDialog = true
                                repository.markNotificationsRead()
                            },
                            modifier = Modifier.testTag("notification_bell_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifs > 0) {
                                        Badge(
                                            containerColor = StatusOverdue,
                                            contentColor = Color.White
                                        ) {
                                            Text("$unreadNotifs")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Taarifa (Notifications)",
                                    tint = LayNavyPrimary
                                )
                            }
                        }

                        // User Avatar
                        UserAvatar(
                            name = user.name,
                            size = 36,
                            backgroundColor = if (isAdmin) LayNavyPrimary else LayGreenDark,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LaySurfaceLight)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    if (isAdmin) {
                        AdminTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = adminSelectedTab == tab,
                                onClick = { adminSelectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = LayNavyPrimary,
                                    selectedTextColor = LayNavyPrimary,
                                    indicatorColor = LayNavyPrimary.copy(alpha = 0.12f),
                                    unselectedIconColor = LayTextTertiary,
                                    unselectedTextColor = LayTextTertiary
                                ),
                                modifier = Modifier.testTag("nav_admin_${tab.name.lowercase()}")
                            )
                        }
                    } else {
                        AgentTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = agentSelectedTab == tab,
                                onClick = { agentSelectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = LayGreenDark,
                                    selectedTextColor = LayGreenDark,
                                    indicatorColor = LayGreenDark.copy(alpha = 0.12f),
                                    unselectedIconColor = LayTextTertiary,
                                    unselectedTextColor = LayTextTertiary
                                ),
                                modifier = Modifier.testTag("nav_agent_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isAdmin) {
                    when (adminSelectedTab) {
                        AdminTab.HOME -> AdminDashboardScreen(
                            repository = repository,
                            onNavigateToAgents = { adminSelectedTab = AdminTab.AGENTS },
                            onNavigateToLoans = { adminSelectedTab = AdminTab.LOANS },
                            onNavigateToLedger = { adminSelectedTab = AdminTab.LEDGER }
                        )
                        AdminTab.AGENTS -> AdminAgentsScreen(repository = repository)
                        AdminTab.LOANS -> AdminLoansScreen(
                            repository = repository,
                            adminName = user.name
                        )
                        AdminTab.LEDGER -> AdminLedgerScreen(repository = repository)
                        AdminTab.ACCOUNT -> AdminAccountScreen(
                            user = user,
                            authRepository = authRepository,
                            repository = repository,
                            onLogout = { }
                        )
                    }
                } else {
                    when (agentSelectedTab) {
                        AgentTab.HOME -> AgentDashboardScreen(
                            agent = user,
                            repository = repository,
                            onNavigateToCustomers = { agentSelectedTab = AgentTab.CUSTOMERS },
                            onNavigateToCollections = { agentSelectedTab = AgentTab.COLLECTIONS },
                            onNavigateToSms = { agentSelectedTab = AgentTab.SMS }
                        )
                        AgentTab.CUSTOMERS -> AgentCustomersScreen(
                            agent = user,
                            repository = repository
                        )
                        AgentTab.COLLECTIONS -> AgentCollectionsScreen(
                            agent = user,
                            repository = repository,
                            onNavigateToCustomers = { agentSelectedTab = AgentTab.CUSTOMERS }
                        )
                        AgentTab.SMS -> AgentSmsScreen(
                            agent = user,
                            repository = repository
                        )
                        AgentTab.ACCOUNT -> AgentAccountScreen(
                            agent = user,
                            authRepository = authRepository,
                            onLogout = { }
                        )
                    }
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = LayNavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kituo cha Taarifa (Notifications)")
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Text("Hakuna taarifa mpya kwa sasa.", color = LayTextSecondary)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = LayTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LayTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = Formatters.formatDateTime(notif.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LayTextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Funga")
                }
            }
        )
    }
}
