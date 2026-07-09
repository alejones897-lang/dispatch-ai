package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DriverEntity
import com.example.data.local.TruckEntity
import com.example.ui.DispatchViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.SuccessGreen

@Composable
fun FleetScreen(
    viewModel: DispatchViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Drivers, 1 = Trucks
    val drivers by viewModel.drivers.collectAsState()
    val trucks by viewModel.trucks.collectAsState()

    var isAddDriverDialogOpen by remember { mutableStateOf(false) }
    var isAddTruckDialogOpen by remember { mutableStateOf(false) }

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
                        text = "Fleet Management",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage equipment, CDL credentials, and staff",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = {
                        if (selectedTab == 0) isAddDriverDialogOpen = true
                        else isAddTruckDialogOpen = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("fleet_register_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (selectedTab == 0) "Driver" else "Truck")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs (Drivers vs Trucks)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth().testTag("fleet_tab_row")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Drivers (${drivers.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Drivers") },
                    modifier = Modifier.testTag("drivers_tab")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Trucks (${trucks.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Trucks") },
                    modifier = Modifier.testTag("trucks_tab")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            if (selectedTab == 0) {
                DriversList(
                    drivers = drivers,
                    trucks = trucks,
                    onDelete = { viewModel.deleteDriver(it) }
                )
            } else {
                TrucksList(
                    trucks = trucks,
                    onDelete = { viewModel.deleteTruck(it) }
                )
            }
        }

        // Add Driver Dialog
        if (isAddDriverDialogOpen) {
            AddDriverDialog(
                trucks = trucks,
                onDismiss = { isAddDriverDialogOpen = false },
                onAdd = { name, phone, cdl, status, location, truckId ->
                    viewModel.createDriver(name, phone, cdl, status, location, truckId)
                    isAddDriverDialogOpen = false
                }
            )
        }

        // Add Truck Dialog
        if (isAddTruckDialogOpen) {
            AddTruckDialog(
                onDismiss = { isAddTruckDialogOpen = false },
                onAdd = { number, trailer, vin, plate, equipment, maintenance ->
                    viewModel.createTruck(number, trailer, vin, plate, equipment, maintenance)
                    isAddTruckDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun DriversList(
    drivers: List<DriverEntity>,
    trucks: List<TruckEntity>,
    onDelete: (DriverEntity) -> Unit
) {
    if (drivers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No drivers logged in active fleet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(drivers, key = { it.id }) { driver ->
                val assignedTruck = trucks.find { it.id == driver.assignedTruckId }
                DriverCard(driver = driver, assignedTruckNumber = assignedTruck?.truckNumber, onDelete = { onDelete(driver) })
            }
        }
    }
}

@Composable
fun TrucksList(
    trucks: List<TruckEntity>,
    onDelete: (TruckEntity) -> Unit
) {
    if (trucks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No trucks logged in active fleet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(trucks, key = { it.id }) { truck ->
                TruckCard(truck = truck, onDelete = { onDelete(truck) })
            }
        }
    }
}

@Composable
fun DriverCard(
    driver: DriverEntity,
    assignedTruckNumber: String?,
    onDelete: () -> Unit
) {
    val statusColor = when (driver.status) {
        "Available" -> SuccessGreen
        "On Trip" -> AmberAccent
        "Off Duty" -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .testTag("driver_card_${driver.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(driver.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = driver.status,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("CDL Information", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(driver.cdlNumber, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Current Location", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(driver.currentLocation, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = "Truck", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assignedTruckNumber?.let { "Assigned Truck: #$it" } ?: "No Truck Assigned",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun TruckCard(
    truck: TruckEntity,
    onDelete: () -> Unit
) {
    val maintenanceColor = when (truck.maintenanceStatus) {
        "Active" -> SuccessGreen
        "Inspection Due" -> AmberAccent
        "In Shop" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .testTag("truck_card_${truck.truckNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Truck Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Truck #${truck.truckNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Trailer: ${truck.trailerNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = maintenanceColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = truck.maintenanceStatus,
                            color = maintenanceColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("VIN Number", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(truck.vin, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Plate & Equipment", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("${truck.plateNumber} | ${truck.equipmentType}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun AddDriverDialog(
    trucks: List<TruckEntity>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, Long?) -> Unit
) {
    var name by remember { mutableStateOf("David Miller") }
    var phone by remember { mutableStateOf("708-555-0112") }
    var cdl by remember { mutableStateOf("IL-CDL-492813") }
    var status by remember { mutableStateOf("Available") }
    var location by remember { mutableStateOf("Chicago, IL") }
    var selectedTruckId by remember { mutableStateOf<Long?>(null) }
    var expandedTruckMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Driver Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Driver Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("add_driver_name"))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cdl, onValueChange = { cdl = it }, label = { Text("CDL License Key") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Current Location City, State") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                // Status
                Text("HOS / Availability Status:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Available", "Off Duty").forEach { st ->
                        val isSelected = status == st
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { status = st },
                            label = { Text(st) }
                        )
                    }
                }

                // Truck dropdown
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    val truckText = trucks.find { it.id == selectedTruckId }?.let { "Truck #${it.truckNumber} (${it.equipmentType})" } ?: "Assign Trailer/Truck"
                    Button(onClick = { expandedTruckMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(truckText)
                    }

                    DropdownMenu(expanded = expandedTruckMenu, onDismissRequest = { expandedTruckMenu = false }) {
                        DropdownMenuItem(text = { Text("No Assigned Truck") }, onClick = {
                            selectedTruckId = null
                            expandedTruckMenu = false
                        })
                        trucks.forEach { tk ->
                            DropdownMenuItem(text = { Text("#${tk.truckNumber} - ${tk.equipmentType}") }, onClick = {
                                selectedTruckId = tk.id
                                expandedTruckMenu = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, phone, cdl, status, location, selectedTruckId) }, modifier = Modifier.testTag("submit_driver_dialog")) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTruckDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String) -> Unit
) {
    var truckNumber by remember { mutableStateOf("TRK-1099") }
    var trailerNumber by remember { mutableStateOf("TRL-9980") }
    var vin by remember { mutableStateOf("1FVACWDB3GP882190") }
    var plateNumber by remember { mutableStateOf("TX-88B-A92") }
    var equipmentType by remember { mutableStateOf("Dry Van") }
    var maintenanceStatus by remember { mutableStateOf("Active") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Fleet Truck") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = truckNumber, onValueChange = { truckNumber = it }, label = { Text("Truck Number") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("add_truck_num"))
                OutlinedTextField(value = trailerNumber, onValueChange = { trailerNumber = it }, label = { Text("Trailer Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("VIN (17 digit code)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = plateNumber, onValueChange = { plateNumber = it }, label = { Text("License Plate Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Text("Equipment Type:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    val equipments = listOf("Dry Van", "Reefer", "Flatbed", "Step Deck")
                    Column {
                        equipments.chunked(2).forEach { rowEq ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowEq.forEach { eq ->
                                    val isSelected = equipmentType == eq
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { equipmentType = eq },
                                        label = { Text(eq, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                Text("Maintenance Status:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Active", "Inspection Due", "In Shop").forEach { ms ->
                        val isSelected = maintenanceStatus == ms
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { maintenanceStatus = ms },
                            label = { Text(ms, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(truckNumber, trailerNumber, vin, plateNumber, equipmentType, maintenanceStatus) }, modifier = Modifier.testTag("submit_truck_dialog")) {
                Text("Register")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
