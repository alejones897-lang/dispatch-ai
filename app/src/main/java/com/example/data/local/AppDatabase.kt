package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "loads")
data class LoadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loadNumber: String,
    val brokerName: String,
    val brokerPhone: String,
    val rateConfirmationAmount: Double,
    val weight: Double, // lbs
    val commodity: String,
    val pickupLocation: String,
    val deliveryLocation: String,
    val pickupDateTime: String,
    val deliveryDateTime: String,
    val status: String, // "Available", "Assigned", "Dispatched", "Delivered"
    val assignedDriverId: Long? = null,
    val assignedTruckId: Long? = null,
    val deadheadMiles: Double = 0.0,
    val estFuelCost: Double = 0.0,
    val eta: String = "",
    val routeDetails: String = "" // AI suggested optimization route
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val cdlNumber: String,
    val status: String, // "Available", "On Trip", "Off Duty"
    val currentLocation: String, // "Houston, TX", etc.
    val assignedTruckId: Long? = null
)

@Entity(tableName = "trucks")
data class TruckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val truckNumber: String,
    val trailerNumber: String,
    val vin: String,
    val plateNumber: String,
    val equipmentType: String, // "Dry Van", "Reefer", "Flatbed", "Step Deck"
    val maintenanceStatus: String // "Active", "In Shop", "Inspection Due"
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "Rate Confirmation", "BOL", "POD", "Driver CDL"
    val loadId: Long? = null,
    val driverId: Long? = null,
    val fileName: String,
    val uploadDate: String,
    val notes: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val senderRole: String, // "Dispatcher", "Driver"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val loadId: Long? = null
)

// --- DAOs ---

@Dao
interface LoadDao {
    @Query("SELECT * FROM loads ORDER BY id DESC")
    fun getAllLoadsFlow(): Flow<List<LoadEntity>>

    @Query("SELECT * FROM loads WHERE id = :id")
    suspend fun getLoadById(id: Long): LoadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoad(load: LoadEntity): Long

    @Update
    suspend fun updateLoad(load: LoadEntity)

    @Delete
    suspend fun deleteLoad(load: LoadEntity)
}

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAllDriversFlow(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :id")
    suspend fun getDriverById(id: Long): DriverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity): Long

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    @Delete
    suspend fun deleteDriver(driver: DriverEntity)
}

@Dao
interface TruckDao {
    @Query("SELECT * FROM trucks ORDER BY truckNumber ASC")
    fun getAllTrucksFlow(): Flow<List<TruckEntity>>

    @Query("SELECT * FROM trucks WHERE id = :id")
    suspend fun getTruckById(id: Long): TruckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTruck(truck: TruckEntity): Long

    @Update
    suspend fun updateTruck(truck: TruckEntity)

    @Delete
    suspend fun deleteTruck(truck: TruckEntity)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY id DESC")
    fun getAllDocumentsFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE loadId = :loadId")
    fun getDocumentsForLoadFlow(loadId: Long): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE loadId = :loadId ORDER BY timestamp ASC")
    fun getMessagesForLoadFlow(loadId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
}

// --- AppDatabase ---

@Database(
    entities = [
        LoadEntity::class,
        DriverEntity::class,
        TruckEntity::class,
        DocumentEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loadDao(): LoadDao
    abstract fun driverDao(): DriverDao
    abstract fun truckDao(): TruckDao
    abstract fun documentDao(): DocumentDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dispatch_ai_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
