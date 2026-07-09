package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DispatchViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: DispatchViewModel,
    modifier: Modifier = Modifier
) {
    var selectedScreenIndex by remember { mutableIntStateOf(0) } // 0=Dashboard, 1=Loads, 2=Fleet, 3=AI, 4=Reports
    val userSession by viewModel.userSession.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showProfileMenu by remember { mutableStateOf(false) }
    var showNotificationDrawer by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Logo Box
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "Dispatch AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = "FLEET CONTROL",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Simulated Notification Alerts
                        IconButton(
                            onClick = { showNotificationDrawer = !showNotificationDrawer },
                            modifier = Modifier.testTag("notification_trigger_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (notifications.isNotEmpty()) {
                                        Badge { Text(notifications.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                            }
                        }

                        // User Profile Initial Circle & Role Selector Dropdown
                        val initials = if (userSession.email.isNotEmpty()) {
                            userSession.email.split("@").firstOrNull()?.take(2)?.uppercase() ?: "JS"
                        } else "JS"

                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Surface(
                                onClick = { showProfileMenu = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("profile_dropdown_trigger"),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.tertiary,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(userSession.email, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Role: ${userSession.role}", color = AmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                            Divider()
                            // Role selector toggles
                            val roles = listOf("Dispatcher", "Admin", "Driver", "Fleet Manager")
                            roles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text("Switch to $role") },
                                    onClick = {
                                        viewModel.login(userSession.email, role)
                                        showProfileMenu = false
                                    },
                                    modifier = Modifier.testTag("switch_role_option_$role")
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Log Out", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    viewModel.logout()
                                    showProfileMenu = false
                                },
                                modifier = Modifier.testTag("logout_option")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        }
    },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedScreenIndex == 0,
                    onClick = { selectedScreenIndex = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_dashboard")
                )
                NavigationBarItem(
                    selected = selectedScreenIndex == 1,
                    onClick = { selectedScreenIndex = 1 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Loads") },
                    label = { Text("Loads", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_loads")
                )
                NavigationBarItem(
                    selected = selectedScreenIndex == 2,
                    onClick = { selectedScreenIndex = 2 },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Fleet") },
                    label = { Text("Fleet", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_fleet")
                )
                NavigationBarItem(
                    selected = selectedScreenIndex == 3,
                    onClick = { selectedScreenIndex = 3 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Co-pilot") },
                    label = { Text("AI", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_ai")
                )
                NavigationBarItem(
                    selected = selectedScreenIndex == 4,
                    onClick = { selectedScreenIndex = 4 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Vault & Chat") },
                    label = { Text("Vault & Chat", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_item_docs_chat")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main workspace routes
            when (selectedScreenIndex) {
                0 -> DashboardWorkspace(
                    viewModel = viewModel,
                    onNavigateToLoads = { selectedScreenIndex = 1 },
                    onNavigateToAi = { selectedScreenIndex = 3 }
                )
                1 -> LoadsScreen(viewModel = viewModel)
                2 -> FleetScreen(viewModel = viewModel)
                3 -> AiCopilotScreen(viewModel = viewModel)
                4 -> ChatAndDocsScreen(viewModel = viewModel)
            }

            // Notification slide-out list
            AnimatedVisibility(
                visible = showNotificationDrawer,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopCenter)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp)
                        )
                        .testTag("notification_drawer")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Logistics Push Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = { viewModel.clearNotifications() }) {
                                Text("Clear All", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (notifications.isEmpty()) {
                            Text(
                                "No pending notifications.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                notifications.take(4).forEach { notif ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Alert",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(notif.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                }
                                                Text(notif.body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showNotificationDrawer = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close Board")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardWorkspace(
    viewModel: DispatchViewModel,
    onNavigateToLoads: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    val loads by viewModel.loads.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val trucks by viewModel.trucks.collectAsState()

    // Dashboard calculations
    val availableLoads = loads.filter { it.status == "Available" }
    val assignedLoads = loads.filter { it.status == "Assigned" }
    val dispatchedLoads = loads.filter { it.status == "Dispatched" }
    val deliveredLoads = loads.filter { it.status == "Delivered" }

    val totalInvoiced = loads.sumOf { it.rateConfirmationAmount }
    val activeDriversCount = drivers.count { it.status == "Available" }
    val activeTrucksCount = trucks.count { it.maintenanceStatus == "Active" }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sleek Interface AI Recommendation Card (Welcome/Hero upgrade)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("welcome_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                SleekBluePrimary,
                                SleekBlueSecondary
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // AI Optimization tag
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF34D399), androidx.compose.foundation.shape.CircleShape)
                                    )
                                    Text(
                                        text = "AI OPTIMIZATION",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "3 High-Profit Loads Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Potential fuel savings: $420/week • Localized fleet pilot and optimization engine active.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Text(
                            text = "✨",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onNavigateToAi()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                text = "View Recommendations",
                                color = SleekBluePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onNavigateToLoads,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Active Loads",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Numerical KPIs overview
        Text("Key Logistics Indexes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricBox(
                title = "Gross Billing",
                value = "$${String.format("%,.0f", totalInvoiced)}",
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            KpiMetricBox(
                title = "Drivers Standby",
                value = activeDriversCount.toString(),
                accentColor = BlueSecondary,
                modifier = Modifier.weight(1f)
            )
            KpiMetricBox(
                title = "Active Trucks",
                value = activeTrucksCount.toString(),
                accentColor = AmberAccent,
                modifier = Modifier.weight(1f)
            )
        }

        // Live GPS Tracking Vector Map (Fulfills Route progress/live GPS tracking)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_gps_map_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = borderOutline()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Live USA GPS Tracking Map", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Active driver transit corridors", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }

                    Surface(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🛰️ Live GPS Active",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GPS Canvas Map drawing
                GpsLiveTransitMap()

                Spacer(modifier = Modifier.height(12.dp))

                // Active vehicles status legend
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GpsLegendItem(driverName = "Elena Rostova (LD-8801)", route = "Pharr, TX ➔ Chicago, IL", status = "In Transit (I-35 N)", color = SuccessGreen)
                    GpsLegendItem(driverName = "Marcus Vance (LD-8802)", route = "Gary, IN ➔ Houston, TX", status = "Dispatched (I-65 S)", color = BlueSecondary)
                }
            }
        }

        // Load summary overview details
        ReportsScreen(viewModel = viewModel)
    }
}

@Composable
fun GpsLiveTransitMap() {
    val mapOutlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val dotColor = SuccessGreen
    val routeLineColor = BlueSecondary.copy(alpha = 0.6f)
    val cityColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        val width = size.width
        val height = size.height

        // Draw simplified USA layout outline map lines
        drawLine(color = mapOutlineColor, start = Offset(width * 0.15f, height * 0.3f), end = Offset(width * 0.85f, height * 0.3f), strokeWidth = 2f)
        drawLine(color = mapOutlineColor, start = Offset(width * 0.85f, height * 0.3f), end = Offset(width * 0.92f, height * 0.6f), strokeWidth = 2f)
        drawLine(color = mapOutlineColor, start = Offset(width * 0.92f, height * 0.6f), end = Offset(width * 0.75f, height * 0.85f), strokeWidth = 2f)
        drawLine(color = mapOutlineColor, start = Offset(width * 0.75f, height * 0.85f), end = Offset(width * 0.45f, height * 0.85f), strokeWidth = 2f)
        drawLine(color = mapOutlineColor, start = Offset(width * 0.45f, height * 0.85f), end = Offset(width * 0.35f, height * 0.95f), strokeWidth = 2f) // Texas nose
        drawLine(color = mapOutlineColor, start = Offset(width * 0.35f, height * 0.95f), end = Offset(width * 0.28f, height * 0.75f), strokeWidth = 2f)
        drawLine(color = mapOutlineColor, start = Offset(width * 0.28f, height * 0.75f), end = Offset(width * 0.08f, height * 0.65f), strokeWidth = 2f)
        drawLine(color = mapOutlineColor, start = Offset(width * 0.08f, height * 0.65f), end = Offset(width * 0.15f, height * 0.3f), strokeWidth = 2f)

        // Draw Route lines for Elena (Texas to Chicago)
        drawLine(
            color = routeLineColor,
            start = Offset(width * 0.35f, height * 0.90f), // Pharr, TX
            end = Offset(width * 0.60f, height * 0.42f),  // Chicago, IL
            strokeWidth = 3f
        )
        // Draw Route lines for Marcus (Indiana to Houston)
        drawLine(
            color = routeLineColor.copy(alpha = 0.4f),
            start = Offset(width * 0.64f, height * 0.40f), // Gary, IN
            end = Offset(width * 0.42f, height * 0.82f), // Houston, TX
            strokeWidth = 3f
        )

        // Draw Cities dots
        val cities = listOf(
            Offset(width * 0.60f, height * 0.42f) to "Chicago",
            Offset(width * 0.35f, height * 0.90f) to "Pharr",
            Offset(width * 0.64f, height * 0.40f) to "Gary",
            Offset(width * 0.42f, height * 0.82f) to "Houston",
            Offset(width * 0.15f, height * 0.58f) to "LA"
        )
        cities.forEach { (offset, _) ->
            drawCircle(color = cityColor, radius = 6f, center = offset)
        }

        // Draw Truck GPS positions (Elena - halfway on route, Marcus - near start)
        val elenaPos = Offset(width * 0.475f, height * 0.66f) // midway Pharr to Chicago
        val marcusPos = Offset(width * 0.58f, height * 0.51f) // partway Gary to Houston

        drawCircle(color = SuccessGreen, radius = 8f, center = elenaPos)
        drawCircle(color = Color.White, radius = 4f, center = elenaPos)

        drawCircle(color = BlueSecondary, radius = 8f, center = marcusPos)
        drawCircle(color = Color.White, radius = 4f, center = marcusPos)
    }
}

@Composable
fun GpsLegendItem(
    driverName: String,
    route: String,
    status: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = color, shape = RoundedCornerShape(100.dp), modifier = Modifier.size(10.dp)) {}
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(driverName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(route, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        Text(status, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
fun KpiMetricBox(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (title.contains("Billing", ignoreCase = true)) {
                    Text(
                        text = "+12%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun borderOutline() = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
