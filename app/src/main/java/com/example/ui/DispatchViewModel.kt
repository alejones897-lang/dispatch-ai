package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.remote.GeminiService
import com.example.data.repository.DispatchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserSession(
    val email: String = "",
    val role: String = "Dispatcher", // "Admin", "Dispatcher", "Driver", "Fleet Manager"
    val isLoggedIn: Boolean = false
)

data class PushNotification(
    val id: Long,
    val title: String,
    val body: String,
    val timestamp: String,
    val read: Boolean = false
)

class DispatchViewModel(
    application: Application,
    private val repository: DispatchRepository
) : AndroidViewModel(application) {

    // User authentication and session
    private val _userSession = MutableStateFlow(UserSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // Database Flows
    val loads: StateFlow<List<LoadEntity>> = repository.allLoads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val drivers: StateFlow<List<DriverEntity>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trucks: StateFlow<List<TruckEntity>> = repository.allTrucks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI dispatch state
    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Simulated Push Notifications Flow
    private val _notifications = MutableStateFlow<List<PushNotification>>(emptyList())
    val notifications: StateFlow<List<PushNotification>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
            // Create a few default notifications for first launch
            _notifications.value = listOf(
                PushNotification(
                    id = 1,
                    title = "System Ready",
                    body = "Dispatch AI database successfully seeded with USA trucking logistics data.",
                    timestamp = "08:00 AM"
                ),
                PushNotification(
                    id = 2,
                    title = "Broker Warning",
                    body = "Heavy congestion reported at Pharr, TX Avocado terminal. Delay expected.",
                    timestamp = "09:15 AM"
                )
            )
        }
    }

    // --- Authentication Actions ---
    fun login(email: String, role: String) {
        _userSession.value = UserSession(email, role, true)
        addNotification("Login Successful", "Signed in as $email with role: $role.")
    }

    fun logout() {
        _userSession.value = UserSession()
    }

    fun register(email: String, role: String) {
        // Simple registration simulation
        login(email, role)
        addNotification("Account Created", "Welcome to Dispatch AI! Signed in as $role.")
    }

    // --- Load Actions ---
    fun createLoad(
        loadNumber: String,
        brokerName: String,
        brokerPhone: String,
        amount: Double,
        weight: Double,
        commodity: String,
        pickup: String,
        delivery: String,
        pickupDate: String,
        deliveryDate: String
    ) {
        viewModelScope.launch {
            val newLoad = LoadEntity(
                loadNumber = loadNumber,
                brokerName = brokerName,
                brokerPhone = brokerPhone,
                rateConfirmationAmount = amount,
                weight = weight,
                commodity = commodity,
                pickupLocation = pickup,
                deliveryLocation = delivery,
                pickupDateTime = pickupDate,
                deliveryDateTime = deliveryDate,
                status = "Available"
            )
            repository.insertLoad(newLoad)
            addNotification("New Load Available", "Load $loadNumber ($commodity) is now available for dispatch.")
        }
    }

    fun updateLoad(load: LoadEntity) {
        viewModelScope.launch {
            repository.updateLoad(load)
        }
    }

    fun deleteLoad(load: LoadEntity) {
        viewModelScope.launch {
            repository.deleteLoad(load)
            addNotification("Load Deleted", "Load ${load.loadNumber} removed from database.")
        }
    }

    fun assignDriverToLoad(loadId: Long, driverId: Long?) {
        viewModelScope.launch {
            val load = repository.getLoadById(loadId)
            if (load != null) {
                if (driverId == null) {
                    // Unassign
                    val updatedLoad = load.copy(
                        assignedDriverId = null,
                        assignedTruckId = null,
                        status = "Available",
                        eta = ""
                    )
                    repository.updateLoad(updatedLoad)
                    addNotification("Load Unassigned", "Load ${load.loadNumber} is now available again.")
                } else {
                    val driver = repository.getDriverById(driverId)
                    val updatedLoad = load.copy(
                        assignedDriverId = driverId,
                        assignedTruckId = driver?.assignedTruckId,
                        status = "Assigned",
                        eta = "Estimated 1 Day"
                    )
                    repository.updateLoad(updatedLoad)

                    // Also change driver status
                    if (driver != null) {
                        repository.updateDriver(driver.copy(status = "On Trip"))
                    }

                    addNotification(
                        "New Load Assigned",
                        "Load ${load.loadNumber} has been assigned to driver ${driver?.name ?: "Unknown"}."
                    )
                }
            }
        }
    }

    fun updateLoadStatus(loadId: Long, newStatus: String) {
        viewModelScope.launch {
            val load = repository.getLoadById(loadId)
            if (load != null) {
                val updatedLoad = load.copy(status = newStatus)
                repository.updateLoad(updatedLoad)

                // If delivered, update driver status back to Available
                if (newStatus == "Delivered" && load.assignedDriverId != null) {
                    val driver = repository.getDriverById(load.assignedDriverId)
                    if (driver != null) {
                        repository.updateDriver(driver.copy(status = "Available"))
                    }
                }

                addNotification(
                    "Status Update",
                    "Load ${load.loadNumber} status updated to: $newStatus"
                )
            }
        }
    }

    // --- Driver Actions ---
    fun createDriver(name: String, phone: String, cdl: String, status: String, location: String, truckId: Long?) {
        viewModelScope.launch {
            val newDriver = DriverEntity(
                name = name,
                phone = phone,
                cdlNumber = cdl,
                status = status,
                currentLocation = location,
                assignedTruckId = truckId
            )
            repository.insertDriver(newDriver)
            addNotification("Driver Registered", "Driver Profile for $name added successfully.")
        }
    }

    fun updateDriver(driver: DriverEntity) {
        viewModelScope.launch {
            repository.updateDriver(driver)
        }
    }

    fun deleteDriver(driver: DriverEntity) {
        viewModelScope.launch {
            repository.deleteDriver(driver)
            addNotification("Driver Removed", "Driver ${driver.name} profile deleted.")
        }
    }

    // --- Truck Actions ---
    fun createTruck(number: String, trailer: String, vin: String, plate: String, equipment: String, maintenance: String) {
        viewModelScope.launch {
            val newTruck = TruckEntity(
                truckNumber = number,
                trailerNumber = trailer,
                vin = vin,
                plateNumber = plate,
                equipmentType = equipment,
                maintenanceStatus = maintenance
            )
            repository.insertTruck(newTruck)
            addNotification("Truck Registered", "Truck #$number ($equipment) added to fleet.")
        }
    }

    fun updateTruck(truck: TruckEntity) {
        viewModelScope.launch {
            repository.updateTruck(truck)
        }
    }

    fun deleteTruck(truck: TruckEntity) {
        viewModelScope.launch {
            repository.deleteTruck(truck)
            addNotification("Truck Removed", "Truck #${truck.truckNumber} deleted.")
        }
    }

    // --- Document Actions ---
    fun uploadDocument(type: String, loadId: Long?, driverId: Long?, fileName: String, notes: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            val todayStr = sdf.format(java.util.Date())
            val doc = DocumentEntity(
                type = type,
                loadId = loadId,
                driverId = driverId,
                fileName = fileName,
                uploadDate = todayStr,
                notes = notes
            )
            repository.insertDocument(doc)
            addNotification("Document Uploaded", "$type document '$fileName' securely archived.")
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }

    // --- Chat Actions ---
    fun sendChatMessage(messageText: String, loadId: Long? = null) {
        val session = _userSession.value
        val senderName = if (session.isLoggedIn) session.email.split("@").first() else "Dispatcher"
        val senderRole = session.role

        viewModelScope.launch {
            val msg = ChatMessageEntity(
                senderName = senderName,
                senderRole = senderRole,
                messageText = messageText,
                loadId = loadId
            )
            repository.insertChatMessage(msg)
        }
    }

    // --- AI Co-Pilot / Assistant Actions ---
    fun askAiAssistant(prompt: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = ""
            val response = GeminiService.getAiSuggestion(prompt)
            _aiResponse.value = response
            _isAiLoading.value = false
            addNotification("AI Co-Pilot Answered", "AI Dispatch assistant provided optimization feedback.")
        }
    }

    fun requestDriverRecommendation(load: LoadEntity) {
        val activeDrivers = drivers.value.filter { it.status == "Available" }
        val driversListStr = activeDrivers.joinToString("\n") { 
            "- ID: ${it.id}, Name: ${it.name}, CDL: ${it.cdlNumber}, Location: ${it.currentLocation}, Status: ${it.status}"
        }
        val prompt = """
            Suggest driver for Load:
            Load Number: ${load.loadNumber}
            Pickup: ${load.pickupLocation}
            Delivery: ${load.deliveryLocation}
            Commodity: ${load.commodity}
            Weight: ${load.weight} lbs
            Rate Confirmation: ${'$'}${load.rateConfirmationAmount}
            
            Available Drivers:
            $driversListStr
            
            Identify the best candidate based on equipment, location, CDL, deadhead miles, and provide estimated fuel cost, ETA, and routing recommendations.
        """.trimIndent()
        askAiAssistant(prompt)
    }

    fun requestRouteOptimization(load: LoadEntity) {
        val prompt = """
            Optimize route, detect deadhead miles, calculate estimated fuel cost, and ETA for:
            Load Number: ${load.loadNumber}
            Commodity: ${load.commodity}
            Weight: ${load.weight} lbs
            Pickup: ${load.pickupLocation}
            Delivery: ${load.deliveryLocation}
            Pickup Time: ${load.pickupDateTime}
            Delivery Time: ${load.deliveryDateTime}
            
            Assume modern diesel trucks. Estimate deadhead empty miles from nearest standard major trucking hubs.
        """.trimIndent()
        askAiAssistant(prompt)
    }

    fun requestConflictAudit() {
        val activeLoads = loads.value.filter { it.status == "Dispatched" || it.status == "Assigned" }
        val activeLoadsStr = activeLoads.joinToString("\n") {
            "- Load ${it.loadNumber} (${it.status}): Pickup: ${it.pickupLocation}, Delivery: ${it.deliveryLocation}, Driver ID: ${it.assignedDriverId}"
        }
        val activeTrucks = trucks.value
        val activeTrucksStr = activeTrucks.joinToString("\n") {
            "- Truck ${it.truckNumber}: Status ${it.maintenanceStatus}, Equipment ${it.equipmentType}"
        }
        val prompt = """
            Run a full schedule conflict audit on active fleet status:
            
            Active Loads & Assignments:
            $activeLoadsStr
            
            Truck Status:
            $activeTrucksStr
            
            Identify any scheduling, HOS rest hours, or maintenance conflicts, and make safety and profitability recommendations.
        """.trimIndent()
        askAiAssistant(prompt)
    }

    // --- Helper for Push Notifications ---
    private fun addNotification(title: String, body: String) {
        val sdf = SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val timeStr = sdf.format(java.util.Date())
        val newNotification = PushNotification(
            id = System.currentTimeMillis(),
            title = title,
            body = body,
            timestamp = timeStr
        )
        _notifications.value = listOf(newNotification) + _notifications.value
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}

class DispatchViewModelFactory(
    private val application: Application,
    private val repository: DispatchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DispatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DispatchViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
