package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LoadEntity
import com.example.ui.DispatchViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.SuccessGreen

@Composable
fun LoadsScreen(
    viewModel: DispatchViewModel,
    modifier: Modifier = Modifier
) {
    val loads by viewModel.loads.collectAsState()
    val drivers by viewModel.drivers.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var selectedLoadForDetails by remember { mutableStateOf<LoadEntity?>(null) }

    // Filter loads
    val filteredLoads = loads.filter { load ->
        val matchesStatus = selectedStatusFilter == "All" || load.status == selectedStatusFilter
        val matchesSearch = load.loadNumber.contains(searchQuery, ignoreCase = true) ||
                load.commodity.contains(searchQuery, ignoreCase = true) ||
                load.pickupLocation.contains(searchQuery, ignoreCase = true) ||
                load.deliveryLocation.contains(searchQuery, ignoreCase = true) ||
                load.brokerName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Freight Load Board",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredLoads.size} loads matched",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = { isCreateDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("create_load_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Load")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by load#, broker, city, commodity...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("search_loads_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Available", "Assigned", "Dispatched", "Delivered")
                filters.forEach { filter ->
                    val isSelected = selectedStatusFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Load List
            if (filteredLoads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "No Loads",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active freight found",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLoads, key = { it.id }) { load ->
                        val assignedDriver = drivers.find { it.id == load.assignedDriverId }
                        LoadItemCard(
                            load = load,
                            assignedDriverName = assignedDriver?.name,
                            onClick = { selectedLoadForDetails = load }
                        )
                    }
                }
            }
        }

        // Create Load Dialog
        if (isCreateDialogOpen) {
            CreateLoadDialog(
                onDismiss = { isCreateDialogOpen = false },
                onCreate = { lNum, bName, bPhone, amt, wt, comm, pick, del, pDate, dDate ->
                    viewModel.createLoad(lNum, bName, bPhone, amt, wt, comm, pick, del, pDate, dDate)
                    isCreateDialogOpen = false
                }
            )
        }

        // Load Details & AI Bottom Sheet Simulation
        if (selectedLoadForDetails != null) {
            LoadDetailsDialog(
                load = selectedLoadForDetails!!,
                viewModel = viewModel,
                onDismiss = { selectedLoadForDetails = null }
            )
        }
    }
}

@Composable
fun LoadItemCard(
    load: LoadEntity,
    assignedDriverName: String?,
    onClick: () -> Unit
) {
    val statusColor = when (load.status) {
        "Available" -> SuccessGreen
        "Assigned" -> AmberAccent
        "Dispatched" -> MaterialTheme.colorScheme.primary
        "Delivered" -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .testTag("load_card_${load.loadNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = load.loadNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${load.commodity}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = load.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pickup & Delivery
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TripOrigin,
                    contentDescription = "Pickup",
                    tint = SuccessGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = load.pickupLocation,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "to",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Delivery",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = load.deliveryLocation,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rate & Weight
                Column {
                    Text(
                        text = "$${String.format("%,.0f", load.rateConfirmationAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SuccessGreen
                    )
                    Text(
                        text = "${String.format("%,.0f", load.weight)} lbs",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Assigned Driver/Truck status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Driver",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = assignedDriverName ?: "Unassigned",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (assignedDriverName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLoadDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Double, Double, String, String, String, String, String) -> Unit
) {
    var loadNumber by remember { mutableStateOf("LD-8804") }
    var brokerName by remember { mutableStateOf("C.H. Robinson") }
    var brokerPhone by remember { mutableStateOf("800-323-7587") }
    var rateAmount by remember { mutableStateOf("2600") }
    var weight by remember { mutableStateOf("40000") }
    var commodity by remember { mutableStateOf("Beverages") }
    var pickupLocation by remember { mutableStateOf("St. Louis, MO") }
    var deliveryLocation by remember { mutableStateOf("Chicago, IL") }
    var pickupDate by remember { mutableStateOf("2026-07-10 07:00") }
    var deliveryDate by remember { mutableStateOf("2026-07-10 17:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Freight Load") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = loadNumber,
                    onValueChange = { loadNumber = it },
                    label = { Text("Load Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_load_number")
                )
                OutlinedTextField(
                    value = commodity,
                    onValueChange = { commodity = it },
                    label = { Text("Commodity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_load_commodity")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rateAmount,
                        onValueChange = { rateAmount = it },
                        label = { Text("Rate ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_load_rate")
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (lbs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_load_weight")
                    )
                }
                OutlinedTextField(
                    value = brokerName,
                    onValueChange = { brokerName = it },
                    label = { Text("Broker Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = brokerPhone,
                    onValueChange = { brokerPhone = it },
                    label = { Text("Broker Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = { pickupLocation = it },
                    label = { Text("Pickup Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deliveryLocation,
                    onValueChange = { deliveryLocation = it },
                    label = { Text("Delivery Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pickupDate,
                    onValueChange = { pickupDate = it },
                    label = { Text("Pickup Date/Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deliveryDate,
                    onValueChange = { deliveryDate = it },
                    label = { Text("Delivery Date/Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        loadNumber,
                        brokerName,
                        brokerPhone,
                        rateAmount.toDoubleOrNull() ?: 0.0,
                        weight.toDoubleOrNull() ?: 0.0,
                        commodity,
                        pickupLocation,
                        deliveryLocation,
                        pickupDate,
                        deliveryDate
                    )
                },
                modifier = Modifier.testTag("submit_load_dialog_button")
            ) {
                Text("Log Load")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadDetailsDialog(
    load: LoadEntity,
    viewModel: DispatchViewModel,
    onDismiss: () -> Unit
) {
    val drivers by viewModel.drivers.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()

    var showAssignDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Load ${load.loadNumber}", fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    viewModel.deleteLoad(load)
                    onDismiss()
                }, modifier = Modifier.testTag("delete_load_btn")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Load", tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status control row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Status: ", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val statuses = listOf("Available", "Assigned", "Dispatched", "Delivered")
                        statuses.forEach { s ->
                            val isSelected = load.status == s
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.updateLoadStatus(load.id, s) }
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Logistics Grid Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Commodity: ${load.commodity}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Weight: ${String.format("%,.0f", load.weight)} lbs", fontSize = 13.sp)
                        Text("Payout: $${String.format("%,.0f", load.rateConfirmationAmount)}", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        Text("Broker: ${load.brokerName} (${load.brokerPhone})", fontSize = 13.sp)
                        Text("Pickup: ${load.pickupLocation} @ ${load.pickupDateTime}", fontSize = 12.sp)
                        Text("Delivery: ${load.deliveryLocation} @ ${load.deliveryDateTime}", fontSize = 12.sp)
                    }
                }

                // Assign Driver Trigger
                Text("Driver Assignment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box {
                    Button(
                        onClick = { showAssignDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("assign_driver_trigger"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        val assignedDriver = drivers.find { it.id == load.assignedDriverId }
                        Icon(Icons.Default.PersonAdd, contentDescription = "Assign")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(assignedDriver?.let { "Assigned: ${it.name}" } ?: "Assign Available Driver")
                    }

                    DropdownMenu(
                        expanded = showAssignDropdown,
                        onDismissRequest = { showAssignDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Unassign Driver") },
                            onClick = {
                                viewModel.assignDriverToLoad(load.id, null)
                                showAssignDropdown = false
                            }
                        )
                        Divider()
                        drivers.forEach { driver ->
                            val isAssigned = load.assignedDriverId == driver.id
                            DropdownMenuItem(
                                text = { Text("${driver.name} - ${driver.status} (${driver.currentLocation})") },
                                onClick = {
                                    viewModel.assignDriverToLoad(load.id, driver.id)
                                    showAssignDropdown = false
                                },
                                modifier = Modifier.testTag("driver_select_option_${driver.name}")
                            )
                        }
                    }
                }

                // AI Co-Pilot Core Actions inside load card
                Text("🤖 AI Dispatch Co-Pilot Integration", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.requestDriverRecommendation(load) },
                        modifier = Modifier.weight(1f).testTag("ai_match_driver_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Suggest Driver", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.requestRouteOptimization(load) },
                        modifier = Modifier.weight(1f).testTag("ai_route_fuel_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Optimize Route", fontSize = 12.sp)
                    }
                }

                // AI Response Area
                AnimatedVisibility(
                    visible = isAiLoading || aiResponse.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI Co-Pilot Dispatch Report", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
                                if (isAiLoading) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (isAiLoading) {
                                Text("Analyzing hours of service (HOS), live truck locations, deadhead miles, and fuel prices...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            } else {
                                Text(
                                    text = aiResponse,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("dismiss_details_btn")) { Text("Done") }
        }
    )
}
