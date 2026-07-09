package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DispatchRepository(private val db: AppDatabase) {

    private val loadDao = db.loadDao()
    private val driverDao = db.driverDao()
    private val truckDao = db.truckDao()
    private val documentDao = db.documentDao()
    private val chatMessageDao = db.chatMessageDao()

    val allLoads: Flow<List<LoadEntity>> = loadDao.getAllLoadsFlow()
    val allDrivers: Flow<List<DriverEntity>> = driverDao.getAllDriversFlow()
    val allTrucks: Flow<List<TruckEntity>> = truckDao.getAllTrucksFlow()
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocumentsFlow()
    val allMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessagesFlow()

    fun getDocumentsForLoad(loadId: Long): Flow<List<DocumentEntity>> = documentDao.getDocumentsForLoadFlow(loadId)
    fun getMessagesForLoad(loadId: Long): Flow<List<ChatMessageEntity>> = chatMessageDao.getMessagesForLoadFlow(loadId)

    suspend fun getLoadById(id: Long): LoadEntity? = withContext(Dispatchers.IO) {
        loadDao.getLoadById(id)
    }

    suspend fun getDriverById(id: Long): DriverEntity? = withContext(Dispatchers.IO) {
        driverDao.getDriverById(id)
    }

    suspend fun getTruckById(id: Long): TruckEntity? = withContext(Dispatchers.IO) {
        truckDao.getTruckById(id)
    }

    suspend fun insertLoad(load: LoadEntity): Long = withContext(Dispatchers.IO) {
        loadDao.insertLoad(load)
    }

    suspend fun updateLoad(load: LoadEntity) = withContext(Dispatchers.IO) {
        loadDao.updateLoad(load)
    }

    suspend fun deleteLoad(load: LoadEntity) = withContext(Dispatchers.IO) {
        loadDao.deleteLoad(load)
    }

    suspend fun insertDriver(driver: DriverEntity): Long = withContext(Dispatchers.IO) {
        driverDao.insertDriver(driver)
    }

    suspend fun updateDriver(driver: DriverEntity) = withContext(Dispatchers.IO) {
        driverDao.updateDriver(driver)
    }

    suspend fun deleteDriver(driver: DriverEntity) = withContext(Dispatchers.IO) {
        driverDao.deleteDriver(driver)
    }

    suspend fun insertTruck(truck: TruckEntity): Long = withContext(Dispatchers.IO) {
        truckDao.insertTruck(truck)
    }

    suspend fun updateTruck(truck: TruckEntity) = withContext(Dispatchers.IO) {
        truckDao.updateTruck(truck)
    }

    suspend fun deleteTruck(truck: TruckEntity) = withContext(Dispatchers.IO) {
        truckDao.deleteTruck(truck)
    }

    suspend fun insertDocument(document: DocumentEntity): Long = withContext(Dispatchers.IO) {
        documentDao.insertDocument(document)
    }

    suspend fun deleteDocument(document: DocumentEntity) = withContext(Dispatchers.IO) {
        documentDao.deleteDocument(document)
    }

    suspend fun insertChatMessage(message: ChatMessageEntity): Long = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(message)
    }

    // --- Seeding / Prepopulate with industry standard USA trucking data ---
    suspend fun prepopulateDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingDrivers = driverDao.getAllDriversFlow().first()
        if (existingDrivers.isNotEmpty()) return@withContext

        // 1. Insert Trucks
        val t1 = truckDao.insertTruck(
            TruckEntity(
                truckNumber = "TRK-2015",
                trailerNumber = "TRL-5390",
                vin = "1FVACWDB2GP102984",
                plateNumber = "TX-99D-H82",
                equipmentType = "Reefer",
                maintenanceStatus = "Active"
            )
        )
        val t2 = truckDao.insertTruck(
            TruckEntity(
                truckNumber = "TRK-4402",
                trailerNumber = "TRL-8823",
                vin = "1FVACWDB8GP301824",
                plateNumber = "IL-44B-K21",
                equipmentType = "Dry Van",
                maintenanceStatus = "Active"
            )
        )
        val t3 = truckDao.insertTruck(
            TruckEntity(
                truckNumber = "TRK-1090",
                trailerNumber = "TRL-1092",
                vin = "1FVACWDB5GP881293",
                plateNumber = "GA-82C-P55",
                equipmentType = "Flatbed",
                maintenanceStatus = "Inspection Due"
            )
        )
        val t4 = truckDao.insertTruck(
            TruckEntity(
                truckNumber = "TRK-7711",
                trailerNumber = "TRL-9988",
                vin = "1FVACWDB4GP409823",
                plateNumber = "CA-21X-Z90",
                equipmentType = "Step Deck",
                maintenanceStatus = "In Shop"
            )
        )

        // 2. Insert Drivers
        val d1 = driverDao.insertDriver(
            DriverEntity(
                name = "Marcus Vance",
                phone = "312-555-0143",
                cdlNumber = "IL-CDL-901827",
                status = "Available",
                currentLocation = "Chicago, IL",
                assignedTruckId = t2
            )
        )
        val d2 = driverDao.insertDriver(
            DriverEntity(
                name = "Elena Rostova",
                phone = "281-555-0182",
                cdlNumber = "TX-CDL-441029",
                status = "On Trip",
                currentLocation = "Dallas, TX",
                assignedTruckId = t1
            )
        )
        val d3 = driverDao.insertDriver(
            DriverEntity(
                name = "Tyrone Miller",
                phone = "404-555-0199",
                cdlNumber = "GA-CDL-881203",
                status = "Available",
                currentLocation = "Atlanta, GA",
                assignedTruckId = t3
            )
        )
        val d4 = driverDao.insertDriver(
            DriverEntity(
                name = "Carlos Santana",
                phone = "213-555-0121",
                cdlNumber = "CA-CDL-201889",
                status = "Off Duty",
                currentLocation = "Los Angeles, CA",
                assignedTruckId = t4
            )
        )

        // Get tomorrow's dates for pickup/delivery
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val dayAfter = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }

        val todayStr = sdf.format(today.time)
        val tomorrowStr = sdf.format(tomorrow.time)
        val dayAfterStr = sdf.format(dayAfter.time)

        // 3. Insert Loads
        val l1 = loadDao.insertLoad(
            LoadEntity(
                loadNumber = "LD-8801",
                brokerName = "C.H. Robinson",
                brokerPhone = "800-323-7587",
                rateConfirmationAmount = 2450.0,
                weight = 42500.0,
                commodity = "Fresh Avocados",
                pickupLocation = "Pharr, TX",
                deliveryLocation = "Chicago, IL",
                pickupDateTime = "$todayStr 08:00",
                deliveryDateTime = "$tomorrowStr 16:00",
                status = "Dispatched",
                assignedDriverId = d2,
                assignedTruckId = t1,
                deadheadMiles = 45.0,
                estFuelCost = 350.0,
                eta = "$tomorrowStr 15:30",
                routeDetails = "I-35 N directly to US-59 N and I-57 N. Clean route, low construction."
            )
        )

        val l2 = loadDao.insertLoad(
            LoadEntity(
                loadNumber = "LD-8802",
                brokerName = "TQL (Total Quality Logistics)",
                brokerPhone = "800-580-3101",
                rateConfirmationAmount = 3100.0,
                weight = 38000.0,
                commodity = "Industrial Steel Coils",
                pickupLocation = "Gary, IN",
                deliveryLocation = "Houston, TX",
                pickupDateTime = "$tomorrowStr 06:00",
                deliveryDateTime = "$dayAfterStr 12:00",
                status = "Assigned",
                assignedDriverId = d1,
                assignedTruckId = t2,
                deadheadMiles = 25.0,
                estFuelCost = 480.0,
                eta = "$dayAfterStr 11:15",
                routeDetails = "I-65 S to I-40 W and I-30 W. Recommended overnight at Little Rock, AR."
            )
        )

        val l3 = loadDao.insertLoad(
            LoadEntity(
                loadNumber = "LD-8803",
                brokerName = "Echo Global Logistics",
                brokerPhone = "800-354-7993",
                rateConfirmationAmount = 1800.0,
                weight = 44000.0,
                commodity = "Paper Rolls",
                pickupLocation = "Savannah, GA",
                deliveryLocation = "Charlotte, NC",
                pickupDateTime = "$tomorrowStr 13:00",
                deliveryDateTime = "$tomorrowStr 20:00",
                status = "Available",
                deadheadMiles = 12.0,
                estFuelCost = 110.0,
                eta = "$tomorrowStr 19:45",
                routeDetails = "I-95 N to I-26 W to I-77 N. Fast transit."
            )
        )

        val l4 = loadDao.insertLoad(
            LoadEntity(
                loadNumber = "LD-8799",
                brokerName = "Landstar System",
                brokerPhone = "800-872-9400",
                rateConfirmationAmount = 4200.0,
                weight = 22000.0,
                commodity = "Medical Hardware",
                pickupLocation = "San Diego, CA",
                deliveryLocation = "Denver, CO",
                pickupDateTime = "2026-07-07 09:00",
                deliveryDateTime = "2026-07-08 17:00",
                status = "Delivered",
                assignedDriverId = d3,
                assignedTruckId = t3,
                deadheadMiles = 60.0,
                estFuelCost = 550.0,
                eta = "Delivered on Time",
                routeDetails = "I-15 N to I-70 E. Heavy elevation grades, but driver handled perfectly."
            )
        )

        // 4. Insert Documents
        documentDao.insertDocument(
            DocumentEntity(
                type = "Rate Confirmation",
                loadId = l1,
                fileName = "Rate_Conf_LD-8801.pdf",
                uploadDate = todayStr,
                notes = "Signed and approved by dispatch."
            )
        )
        documentDao.insertDocument(
            DocumentEntity(
                type = "BOL",
                loadId = l1,
                fileName = "BOL_LD-8801_PHARR.pdf",
                uploadDate = todayStr,
                notes = "Signed at pickup location Pharr Avocado Terminal."
            )
        )
        documentDao.insertDocument(
            DocumentEntity(
                type = "Driver CDL",
                driverId = d1,
                fileName = "Marcus_Vance_CDL_IL.jpg",
                uploadDate = "2026-06-15",
                notes = "Class A CDL with Double/Triple and Tanker endorsements."
            )
        )

        // 5. Insert Chat Messages
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                senderName = "Marcus Vance",
                senderRole = "Driver",
                messageText = "Hi team, I'm heading over to Gary, IN for the steel coils pickup tomorrow morning. Anything else I should know?",
                loadId = l2
            )
        )
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                senderName = "Dispatch Main",
                senderRole = "Dispatcher",
                messageText = "Hi Marcus! Perfect. TQL wants us to call 1 hour before pickup. I will handle that. BOL number is already attached. Have a safe drive!",
                loadId = l2
            )
        )
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                senderName = "Elena Rostova",
                senderRole = "Driver",
                messageText = "Just crossed the Illinois border. Avocados are cooled at 38°F steady. ETA to Chicago receiver remains tomorrow afternoon.",
                loadId = l1
            )
        )
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                senderName = "Dispatch Main",
                senderRole = "Dispatcher",
                messageText = "Excellent report Elena. Stay safe, we have a reload ready for you from Chicago back to Laredo on Friday!",
                loadId = l1
            )
        )
    }
}
